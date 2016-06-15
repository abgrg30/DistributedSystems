package storage;

import java.io.*;
import java.net.*;

import common.*;
import rmi.*;
import naming.*;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{

    private File root;

    TestSkeleton<Storage> storageSkeleton;
    TestSkeleton<Command> commandSkeleton;
    private volatile boolean clientStopped = false;
    private volatile boolean commandStopped = false;

    //private Skeleton<Storage> storageSkeleton;
    //private Skeleton<Command> commandSkeleton;

    /* TestSkeleton is a wrapper class that is used to suppress
       Xlint warnings.
    */
    private class TestSkeleton<T> extends Skeleton<T>
    {
        protected final StorageServer  test;
        private boolean                stopped = false;

        TestSkeleton(Class<T> remote_interface, T server, StorageServer test)
        {
            super(remote_interface, server);
            this.test = test;
        }

        TestSkeleton(Class<T> remote_interface, T server, InetSocketAddress address,
                     StorageServer test)
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


    /** Creates a storage server, given a directory on the local filesystem, and
        ports to use for the client and command interfaces.

        <p>
        The ports may have to be specified if the storage server is running
        behind a firewall, and specific ports are open.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @param client_port Port to use for the client interface, or zero if the
                           system should decide the port.
        @param command_port Port to use for the command interface, or zero if
                            the system should decide the port.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */
    public StorageServer(File root, int client_port, int command_port)
    {
        if (root == null) {
            throw new NullPointerException("Root cannot be null");
        }
        this.root = root.getAbsoluteFile();


        if (client_port == 0) {
            storageSkeleton = new TestSkeleton<Storage>(Storage.class, this, this);
        } else {
            InetSocketAddress address = new InetSocketAddress(client_port);
            storageSkeleton = new TestSkeleton<Storage>(Storage.class, this, address, this);
        }

        if (command_port == 0) {
            commandSkeleton = new TestSkeleton<Command>(Command.class, this, this);
        } else {
            InetSocketAddress address = new InetSocketAddress(command_port);
            commandSkeleton = new TestSkeleton<Command>(Command.class, this, address, this);
        }
    }

    /** Creats a storage server, given a directory on the local filesystem.

        <p>
        This constructor is equivalent to
        <code>StorageServer(root, 0, 0)</code>. The system picks the ports on
        which the interfaces are made available.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
     */
    public StorageServer(File root)
    {
        this(root, 0, 0);
    }

    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    public synchronized void start(String hostname, Registration naming_server)
        throws RMIException, UnknownHostException, FileNotFoundException
    {
        if (!root.exists()) {
            throw new FileNotFoundException("Server directory does not exist");
        }

        if (!root.isDirectory()) {
            throw new FileNotFoundException("Root is not a directory");
        }

        // Start the storage server and command server.
        // Will throw RMIException if skeleton could not be started.
        storageSkeleton.start();
        commandSkeleton.start();

        // Create stubs and register with the naming server
        // Will throw UnknownHostException if stub could not be created.
        Storage storageStub = Stub.create(Storage.class, storageSkeleton, hostname);
        Command commandStub = Stub.create(Command.class, commandSkeleton, hostname);

        // Get the list of files that need to be removed.
        Path[] filePaths = naming_server.register(storageStub, commandStub, Path.list(root));

        for (Path p : filePaths) {
            delete(p);
        }

        removeEmptyDirs(root);
    }

    // A helper function to recursively delete Empty Directories.
    private synchronized void removeEmptyDirs(File dir) {
        if (!dir.isDirectory()) {
            return;
        }

        for (File f : dir.listFiles()) {
            removeEmptyDirs(f);
        }

        // Did we leave this empty directory?
        if (dir.list().length == 0) {
            dir.delete();
        }
    }

    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
        storageSkeleton.stop();
        commandSkeleton.stop();
    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following methods are documented in Storage.java.
    /** Returns the length of a file, in bytes.

        @param file Path to the file.
        @return The length of the file.
        @throws FileNotFoundException If the file cannot be found or the path
                                      refers to a directory.
        @throws RMIException If the call cannot be completed due to a network
                             error.
     */
    @Override
    public synchronized long size(Path file) throws FileNotFoundException
    {
        File f = file.toFile(root);

        if (!f.exists()) {
            throw new FileNotFoundException(f.getName() + " does not exist");
        }

        if (f.isDirectory()) {
            throw new FileNotFoundException(f.getName() + " is a directory");
        }

        return f.length();
    }


    /** Reads a sequence of bytes from a file.

        @param file Path to the file.
        @param offset Offset into the file to the beginning of the sequence.
        @param length The number of bytes to be read.
        @return An array containing the bytes read. If the call succeeds, the
                number of bytes read is equal to the number of bytes requested.
        @throws IndexOutOfBoundsException If the sequence specified by
                                          <code>offset</code> and
                                          <code>length</code> is outside the
                                          bounds of the file, or if
                                          <code>length</code> is negative.
        @throws FileNotFoundException If the file cannot be found or the path
                                      refers to a directory.
        @throws IOException If the file read cannot be completed on the server.
        @throws RMIException If the call cannot be completed due to a network
                             error.
     */
    @Override
    public synchronized byte[] read(Path file, long offset, int length)
        throws FileNotFoundException, IOException
    {
        File f = file.toFile(root);

/*
        if (!f.exists()) {
            throw new FileNotFoundException(f.getName() + " does not exist");
        }
*/

        if (f.isDirectory()) {
            throw new FileNotFoundException(f.getName() + " is a directory");
        }

        if (!f.canRead()) {
            throw new FileNotFoundException(f.getName() + " cannot be read");
        }

        if (offset < 0 ||
            offset > Integer.MAX_VALUE ||
            length < 0 ||
            offset + length > f.length()) {
              throw new IndexOutOfBoundsException("Offset or/and length out of bounds");
        }

        RandomAccessFile reader = new RandomAccessFile(f, "r");
        reader.seek(offset);

        byte[] buf = new byte[length];
        reader.readFully(buf);

        reader.close();

        return buf;
    }

    /** Writes bytes to a file.

        @param file Path to the file.
        @param offset Offset into the file where data is to be written.
        @param data Array of bytes to be written.
        @throws IndexOutOfBoundsException If <code>offset</code> is negative.
        @throws FileNotFoundException If the file cannot be found or the path
                                      refers to a directory.
        @throws IOException If the file write cannot be completed on the server.
        @throws RMIException If the call cannot be completed due to a network
                             error.
     */
    @Override
    public synchronized void write(Path file, long offset, byte[] data)
        throws FileNotFoundException, IOException
    {
        File f = file.toFile(root);


        if (!f.exists()) {
            throw new FileNotFoundException(f.getName() + " does not exist");
        }

        if (f.isDirectory()) {
            throw new FileNotFoundException(f.getName() + " is a directory");
        }

        if (!f.canWrite()) {
            throw new FileNotFoundException(f.getName() + " cannot be written to");
        }

        if (offset < 0) {
            throw new IndexOutOfBoundsException("Offset is negative");
        }

        RandomAccessFile writer = new RandomAccessFile(f, "rw");
        writer.seek(offset);
        writer.write(data);
        writer.close();
    }

    // The following methods are documented in Command.java.
    /** Creates a file on the storage server.

        @param file Path to the file to be created. The parent directory will be
                    created if it does not exist. This path may not be the root
                    directory.
        @return <code>true</code> if the file is created; <code>false</code>
                if it cannot be created.
        @throws RMIException If the call cannot be completed due to a network
                             error.
     */
    @Override
    public synchronized boolean create(Path file)
    {
        // cannot create the root directory.
        if (file.isRoot()) {
            return false;
        }

        File parent = file.parent().toFile(root);
        parent.mkdirs();

        File f = file.toFile(root);

        try {
            return f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Deletes a file or directory on the storage server.

        <p>
        If the file is a directory and cannot be deleted, some, all, or none of
        its contents may be deleted by this operation.

        @param path Path to the file or directory to be deleted. The root
                    directory cannot be deleted.
        @return <code>true</code> if the file or directory is deleted;
                <code>false</code> otherwise.
        @throws RMIException If the call cannot be completed due to a network
                             error.
     */
    @Override
    public synchronized boolean delete(Path path)
    {
        // cannot delete the root directory.
        if (path.isRoot()) {
            return false;
        }

        if (!path.toFile(root).exists()) {
            return false;
        }

        boolean result = doDelete(path.toFile(root));
        removeEmptyDirs(root);

        return result;
    }

    private boolean doDelete(File file) {
        boolean test = true;

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                // Capture every delete result.
                test = test && doDelete(f);
            } /* for */
        } /* if */

        return (test && file.delete());
    }

    /** Copies a file from another storage server.

        @param file Path to the file to be copied.
        @param server Storage server from which the file is to be downloaded.
        @return <code>true</code> if the file is successfully copied;
                <code>false</code> otherwise.
        @throws FileNotFoundException If the file is not present on the remote
                                      storage server, or the path refers to a
                                      directory.
        @throws IOException If an I/O exception occurs either on the remote or
                            on this storage server.
        @throws RMIException If the call cannot be completed due to a network
                             error, whether between the caller and this storage
                             server, or between the two storage servers.
     */
    @Override
    public synchronized boolean copy(Path file, Storage server)
        throws RMIException, FileNotFoundException, IOException
    {
        if (file == null || server == null) {
            throw new NullPointerException("Null parameter");
        }

        server.read(file, 0, 1);

        // If this file already exists, delete it.
        if (file.toFile(root).exists()) {
            delete(file);
        }

        if (!create(file)) {
            throw new IOException("File creation failed");
        }

        long fileSize = server.size(file);

        int READ_SIZE = 4 * 1024; // 4K

        int offset = 0;
        int length = READ_SIZE;

        while (fileSize > 0) {
            if (fileSize < length) {
                length = (int)fileSize;
            }
            byte[] buf = new byte[length];

            buf = server.read(file, offset, length);
            write(file, offset, buf);

            fileSize -= length;
            offset += length;
        } /* end of while */

        return true;

    } /* end of copy */
}
