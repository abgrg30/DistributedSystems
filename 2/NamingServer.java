package naming;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import rmi.*;
import common.*;
import storage.*;


/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{
    /* TestSkeleton is a wrapper class that is used to suppress
       Xlint warnings.
    */
    private class TestSkeleton<T> extends Skeleton<T>
    {
        protected final NamingServer   test;
        private boolean                stopped = false;

        TestSkeleton(Class<T> remote_interface, T server, NamingServer test)
        {
            super(remote_interface, server);
            this.test = test;
        }

        TestSkeleton(Class<T> remote_interface, T server, InetSocketAddress address,
                     NamingServer test)
        {
            super(remote_interface, server, address);
            this.test = test;
        }

        @Override
        protected synchronized void stopped(Throwable cause)
        {
            stopped = true;
        }
    } /* end of private class TestSkeleton */

    /* A private class that is used to save both the
     * Storage and Command stubs.
     */
    private class stubPair {
        private Storage storage;
        private Command command;

        public stubPair(Storage storage, Command command) {
            this.storage = storage;
            this.command = command;
        }

        public Storage getStorage() {
            return storage;
        }

        public Command getCommand() {
            return command;
        }

        /*
        * Needed to compare stubPair objects.
        * Prevents duplication registration of stubs with the
        * NamingServer.
        */
        @Override
        public boolean equals(Object o) {
            return this.storage.equals(((stubPair)o).getStorage()) &&
                   this.command.equals(((stubPair)o).getCommand());
        }
    }

    /*
     * Private class for representing files.
     */
    private class fileNode {
        ConcurrentHashMap<String, fileNode> fileSet;
        String                              pathElement;
        boolean                             isFilePathValid;

        private volatile Vector<stubPair> stubs;

        public fileNode(String pathElement) {
            fileSet           = new ConcurrentHashMap<String, fileNode>();
            this.pathElement  = pathElement;
            isFilePathValid   = false;
            stubs             = new Vector<stubPair>();
        }

        public fileNode(String pathElement, stubPair sp) {
            fileSet           = null;
            this.pathElement  = pathElement;
            isFilePathValid   = true;
            stubs             = new Vector<stubPair>();
            synchronized(this) {
                this.stubs.add(sp);
            }
        }

        // Getters

        public synchronized ConcurrentHashMap<String, fileNode> getFileSet() {
            return fileSet;
        }

        public synchronized String getPathElement() {
            return pathElement;
        }

        public synchronized boolean isFilePathValid() {
            return isFilePathValid;
        }

        public synchronized List<stubPair> getStubs() {
            return stubs;
        }

        // More helper functions for fileNode

        public synchronized fileNode getFile(String file) {
            return  fileSet.get(file);
        }

        public synchronized fileNode addFile(String file, fileNode fileNode) {
            return  fileSet.put(file, fileNode);
        }

        public synchronized void removeFile(Path filePath) {
            fileSet.remove(filePath.toString());
        }

        // More helper functions for stubPairs for fileNode.

        public synchronized stubPair getStubPair() {
            int index = new Random().nextInt(stubs.size());
            return stubs.get(index);
        }

        public synchronized void addStubPair(stubPair sp) {
            stubs.add(sp);
        }

        public synchronized void removeStubPair(stubPair sp) {
            stubs.remove(sp);
        }
    }

    /* Simple read write lock.
     * This read-write lock allows multiple readers and a single writer.
     */
    public class rwLock {

        private int readers    = 0;
        private int waiters    = 0; /* Waiting for write lock */
        private boolean writer = false;

        /* We can read if
         * - there is no one writing to this file.
         * - there is no one waiting for write locks.
         */
        private boolean canRead() {
            if (hasWriter() || hasWaiter()) {
                return false;
            }
            return true;
        }

        /*
         * We can write if
         * - there is no one writing to this file.
         * - there are no threads reading this file.
         */
        private boolean canWrite() {
            if (hasWriter() || hasReaders()) {
                return false;
            }
            return true;
        }


        private boolean hasReaders() {
            return (readers > 0);
        }

        private boolean hasWriter() {
            return writer;
        }

        private boolean hasWaiter() {
            return (waiters > 0);
        }

        // lock and unlock routines

        public synchronized void lockReader() throws InterruptedException {
            while (!canRead()) {
                wait();
            }
            readers++;
        }

        public synchronized void unlockReader() throws InterruptedException {
            readers--;
            notifyAll();
        }

        public synchronized void lockWriter() throws InterruptedException {
            waiters++;
            while(!canWrite()) {
                wait();
            }
            waiters--;
            writer = true;
        }

        public synchronized void unlockWriter() throws InterruptedException {
            writer = false;
            notifyAll();
        }
    } /* end of rwLock class */

    /*
     * A class that takes care of replication work for a file.
     */
    private class replWorker extends Thread {
        Path filePath;

        replWorker(Path path) {
            filePath = path;
        }

        public synchronized void run() {
            Integer replCount = replCounter.get(filePath);

            // Set replCount to zero to prevent duplicate request to copy.
            replCounter.put(filePath, 0);

            // Try to lock the file. This is not an exlusive lock.
            try {
                lock(filePath, false);
            } catch (FileNotFoundException e) {
                return;
            }

            // Get the fileNode for this file.
            fileNode node = getNode(filePath);
            if (node == null) {
                // We should never get here since we locked the file before this.
                // This is a sanity check only.
                return;
            }

            // Find a stub pair which is registered with the NamingServer
            // on which we have not yet replicated this file.
            List<stubPair> stubs    = node.getStubs();
            List<stubPair> newStubs = new ArrayList<stubPair>();

            for (stubPair s : stubList) {
                if (!stubs.contains(s)) {
                    newStubs.add(s);
                }
            }

            // If we found no such stubPair, return without replicating.
            if (newStubs.size() > 0) {
                stubPair[] stubArray = newStubs.toArray(new stubPair[newStubs.size()]);

                // Select a stubPair at random.
                int index            = new Random().nextInt(stubArray.length);
                stubPair sp          = stubArray[index];

                try {
                    // Copy the file.
                    if (sp.getCommand().copy(filePath, node.getStubPair().getStorage())) {
                        node.addStubPair(sp);
                        replCounter.put(filePath, 0);
                    } else {
                        replCounter.put(filePath, replCount);
                    }
                } catch (FileNotFoundException e) {
                    // Do nothing
                } catch (RMIException e) {
                    // Do nothing
                } catch (IOException e) {
                    // Do nothing
                }
            } /* end if */

            // Unlock the file
            unlock(filePath, false);
        } /* end of run function */

    } /* end of replWorker class */

    /*  Class that is used to delete file copies.
     */
    private class clearCopies extends Thread {
        Path filePath;

        clearCopies(Path path) {
            filePath = path;
        }

        public synchronized void run() {
            Integer replCount = replCounter.get(filePath);
            if (replCount == 0) {
                return;
            }

            try {
                lock(filePath, true);
            } catch (FileNotFoundException e) {
                return;
            }

            fileNode fnode = getNode(filePath);
            if (fnode == null) {
                // We should never get here since we locked the file before this.
                // This is a sanity check only.
                return;
            }

            ArrayList<stubPair> newStubs = new ArrayList<stubPair>();
            for (stubPair sp : fnode.getStubs()) {
                newStubs.add(sp);
            }

            if (newStubs.size() > 1) {
                stubPair[] stubArray = newStubs.toArray(new stubPair[newStubs.size()]);
                int index            = new Random().nextInt(stubArray.length);
                stubPair stubP       = stubArray[index];

                newStubs.remove(stubP);

                synchronized (fileRoot) {
                    for (stubPair sp : newStubs) {
                        try {
                            sp.getCommand().delete(filePath);
                        } catch (RMIException e) {
                            // Do nothing
                        }
                        fnode.removeStubPair(sp);
                    } /* end for */
                }
              } /* end if */

              replCounter.put(filePath, 0);
              unlock(filePath, true);
         } /* end run */

    } /* end clearCopies */


    // The Skeletons used for RMI communications
    private TestSkeleton<Service>       serviceSkeleton;
    private TestSkeleton<Registration>  registrationSkeleton;

    // Storage and Comamnd Stubs
    private volatile Vector<stubPair>   stubList;

    // file system root
    private fileNode                    fileRoot;

    private volatile ConcurrentHashMap<Path, rwLock>  lockList;
    private volatile ConcurrentHashMap<Path, Integer> replCounter;

    // Private helper functions.
    private List<Path> getParents(Path path) {
        ArrayList<Path> parentPaths = new ArrayList<Path>();
        parentPaths.add(path);

        while(true) {
            try {
                parentPaths.add(path.parent());
                path = path.parent();
            } catch (IllegalArgumentException e) {
                break;
            }
        } /* end while */

        return parentPaths;
    } /* end getParents function */

    private fileNode getNode(Path path) {
        fileNode node = fileRoot;

        for (String p : path) {
            node = node.getFile(p);
            if (node == null) {
                return null;
            }
        } /* end for*/

        return node;
    } /* end getNode function */


    private void getWriterLock(Path p) throws FileNotFoundException {
        try {
            if (!isDirectory(p)) {
                synchronized(NamingServer.this) {
                    Integer replCount = replCounter.get(p);
                    if (replCount == null) {
                        replCounter.put(p , 1);
                    }
                    clearCopies cTid = new clearCopies(p);
                    cTid.start();
                }
            }
            lockList.get(p).lockWriter();
        } catch (InterruptedException e) {
            return;
        }
    }

    private void getReaderLock(Path p) throws FileNotFoundException {
        try {
            lockList.get(p).lockReader();
            if (!isDirectory(p)) {
                synchronized(NamingServer.this) {
                    Integer replCount = replCounter.get(p);
                    if (replCount == null) {
                        replCounter.put(p, 1);
                    } else if (replCounter.get(p) >= 2) {
                        replWorker rTid = new replWorker(p);
                        rTid.start();
                    }
                    replCounter.put(p, replCounter.get(p) + 1);
                }
            } /* end if */
        } catch (InterruptedException e) {
          return;
        } /* end catch */
    }

    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
        InetSocketAddress serviceAddress      = new InetSocketAddress(NamingStubs.SERVICE_PORT);
        InetSocketAddress registrationAddress = new InetSocketAddress(NamingStubs.REGISTRATION_PORT);

        fileRoot             = new fileNode("");

        serviceSkeleton      = new TestSkeleton<Service>(Service.class, this, serviceAddress, this);
        registrationSkeleton = new TestSkeleton<Registration>(Registration.class, this, registrationAddress, this);

        stubList             = new Vector<stubPair>();

        lockList             = new ConcurrentHashMap<Path, rwLock>();
        replCounter          = new ConcurrentHashMap<Path, Integer>();

    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
        serviceSkeleton.start();
        registrationSkeleton.start();
    }

    /** Stops the naming server.

        <p>
        This method commands both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
        ThreadGroup tGrp = Thread.currentThread().getThreadGroup();
        if (tGrp != null) {
            tGrp.interrupt();
        }

        serviceSkeleton.stop();
        registrationSkeleton.stop();
        this.stopped(null);
    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following public methods are documented in Service.java.
    /*
     * Locking a file requires us to lock all the parent directories in the
     * file path. For a read lock, we read lock all the parents and file as well.
     * For write lock, we only read lock all the parent directories and write
     * lock this file. Fairness is guaranteed by semantics of rwLock. Progress
     * is guaranteed because of thread wait semantics (hopefully).
     * To prevent deadlocks, I am locking the file after sorting the parent
     * directory paths. This ensures that we always obtain lock in specific order.
     * (Handles the lock deadlock case decribed in Path.java)
    */
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException
    {
        if (path == null) {
            throw new NullPointerException("File path cannot be null");
        }

        if (getNode(path) == null) {
            throw new FileNotFoundException("Invalid file path");
        }

        List<Path> parentPaths = getParents(path);
        for (Path p : parentPaths) {
            if (lockList.get(p) == null) {
                lockList.put(p, new rwLock());
            }
        }

        Collections.sort(parentPaths);

        int readLocks = parentPaths.size();
        if (exclusive) {
            readLocks = parentPaths.size() - 1;
        }

        for (int i = 0; i < readLocks; i++) {
            getReaderLock(parentPaths.get(i));
        } /* end for */

        // Write lock the file if exclusive flag is set.
        if (exclusive) {
            getWriterLock(parentPaths.get(parentPaths.size() - 1));
        }

    } /* end function lock */

    @Override
    public void unlock(Path path, boolean exclusive)
    {
        if (path == null) {
            throw new NullPointerException("File path cannot be null");
        }

        if(getNode(path) == null) {
            throw new IllegalArgumentException("Invalid file path");
        }

        List<Path> parentPaths = getParents(path);
        Collections.sort(parentPaths);

        int readLocks = parentPaths.size();
        if (exclusive) {
            readLocks = parentPaths.size() - 1;
        }

        for (int i = 0; i < readLocks; i++) {
            try {
                lockList.get(parentPaths.get(i)).unlockReader();
            } catch (InterruptedException e) {
                return;
            }
        } /* end for */

        // Write unlock the file if exclusive flag is set.
        if (exclusive) {
            try {
                lockList.get(parentPaths.get(parentPaths.size() - 1)).unlockWriter();
            } catch (InterruptedException e) {
                return;
            }
        }

    } /* end function unlock */

    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
        fileNode node = getNode(path);
        if (node == null) {
            throw new FileNotFoundException(path.toString() + " not found");
        }

        return !node.isFilePathValid();
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
        if(!isDirectory(directory)) {
            throw new FileNotFoundException("Path is not a directory");
        }

        // Lock directory before reading all file list.
        lock(directory, false);

        fileNode node = getNode(directory);
        if (node == null) {
            throw new FileNotFoundException("Directory not found");
        }

        Set<String> fileSet = node.getFileSet().keySet();

        // unlock directory.
        unlock(directory, false);

        return fileSet.toArray(new String[fileSet.size()]);
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
        if (file == null) {
            throw new NullPointerException("File path cannot be null");
        }

        if (file.isRoot()) {
            return false;
        }

        if (!isDirectory(file.parent())) {
            throw new FileNotFoundException("Could not find the parent directory");
        }

        fileNode parentDir = fileRoot;
        fileNode node      = null;

        for (String p : file) {
            node = parentDir.getFile(p);

            if (node == null) {
                stubPair[] sl = stubList.toArray(new stubPair[stubList.size()]);
                int index     = new Random().nextInt(sl.length);
                stubPair sp   = sl[index];
                sp.getCommand().create(file);
                parentDir.addFile(p, new fileNode(p, sp));
                return true;
            }

            if (node.isFilePathValid()) {
                return false;
            }

            if (node.getPathElement().equals(file.last())) {
                return false;
            }

            parentDir = node;
        }

        return false;
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {

        if (directory == null) {
            throw new NullPointerException("Directory path is null");
        }

        if (directory.isRoot()) {
            return false;
        }

        if (!isDirectory(directory.parent())) {
            throw new FileNotFoundException("Directory not found");
        }

        fileNode parentDir = fileRoot;
        fileNode node      = null;

        for (String p : directory) {
            node = parentDir.getFile(p);

            if (node == null) {
                parentDir.addFile(p, new fileNode(p));
                return true;
            }

            if (node.isFilePathValid()) {
                return false;
            }

            if(node.getPathElement() == null) {
                return false;
            }

            if (node.getPathElement().equals(directory.last())) {
                return false;
            }

            parentDir = node;
        }
        return false;
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException, RMIException
    {
        fileNode node = getNode(path);
        if (node == null) {
            throw new FileNotFoundException("Could not find file to delete");
        }

        lock(path, true);
        boolean status = true;

        if (isDirectory(path)) {
            synchronized (stubList) {
                for (stubPair s : stubList) {
                    status = status && (s.getCommand().delete(path));
                }
                getNode(path.parent()).removeFile(path);
            }
        } else {
            synchronized (fileRoot) {
                for (stubPair s : node.getStubs()) {
                    status = status && (s.getCommand().delete(path));
                }
                node.stubs.clear();
                getNode(path.parent()).removeFile(path);
            }
        }
        unlock(path, true);
        return status;
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        fileNode node = getNode(file);
        if (node == null) {
            throw new FileNotFoundException("File not found");
        }

        if(!node.isFilePathValid()) {
            throw new FileNotFoundException("File not found");
        }

        return node.getStubPair().getStorage();
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
        if (client_stub == null) {
            throw new NullPointerException("Storage stub cannot be null");
        }

        if (command_stub == null) {
            throw new NullPointerException("Command stub cannot be null");
        }

        if (files == null) {
            throw new NullPointerException("Files cannot be null");
        }

        stubPair sp = new stubPair(client_stub, command_stub);

        synchronized(this) {
            if (stubList.contains(sp)) {
                throw new IllegalStateException("Duplicate registration");
            }
            stubList.add(sp);
        }

        ArrayList<Path> duplicateFiles = new ArrayList<Path>();

        for (int i = 0 ; i < files.length ; i++) {
            fileNode node       = fileRoot;
            boolean isDuplicate = false;
            for (String p : files[i]) {
                node = node.getFile(p);
                if (node == null) {
                    break;
                }

                if (p.equals(files[i].last())) {
                    isDuplicate = true;
                }
            } /* end for */

            if (isDuplicate) {
                duplicateFiles.add(files[i]);
                continue;
            } /* end if */

            fileNode parentNode = fileRoot;
            for (String p : files[i]) {
                node = parentNode.getFile(p);
                if (node == null) {
                    fileNode curr;
                    if (p.equals(files[i].last())) {
                        curr = new fileNode(p, sp);
                    } else {
                        curr = new fileNode(p);
                    }
                    parentNode.addFile(p, curr);
                } /* end if */
                parentNode = parentNode.getFile(p);
            } /* end for */
        } /* end for */
        return duplicateFiles.toArray(new Path[duplicateFiles.size()]);
    }
}
