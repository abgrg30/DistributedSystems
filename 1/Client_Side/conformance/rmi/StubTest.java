package conformance.rmi;

import test.*;
import rmi.*;
import java.net.*;

public class StubTest extends Test
{
    public static final String  notice =
        "checking that stubs attempt connections";

    private ServerSocket                listen_socket = null;
    private InetSocketAddress           address =
        new InetSocketAddress(TestConstants.PORT);
    private boolean                     connected;
    private boolean                     cancel = false;

    @Override
    protected void initialize() throws TestFailed
    {
        try
        {
            listen_socket = new ServerSocket();
            listen_socket.bind(address);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to bind listening socket", t);
        }

        new Thread(new ServerThread()).start();
    }

    @Override
    protected void perform() throws TestFailed
    {
        Skeleton<SimpleInterface>       dummy_skeleton;
        SimpleInterface                 explicit_stub;
        SimpleInterface                 implicit_stub;

        task("creating dummy skeleton");

        try
        {
            dummy_skeleton =
                new Skeleton<SimpleInterface>(SimpleInterface.class,
                                              new SimpleServer(), address);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create test skeleton", t);
        }

        task("creating stub with explicit address");

        try
        {
            explicit_stub = Stub.create(SimpleInterface.class, address);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub with an explicit " +
                                 "address", t);
        }

        task("creating stub with address taken from skeleton");

        try
        {
            implicit_stub = Stub.create(SimpleInterface.class, dummy_skeleton);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub from a skeleton", t);
        }

        task("connecting to server using stub with explicit address");

        checkStubConnects(explicit_stub);

        task("connecting to server using stub with address assigned from " +
             "skeleton");

        checkStubConnects(implicit_stub);

        task();
    }

    @Override
    protected void clean()
    {
        if(listen_socket != null)
        {
            try
            {
                listen_socket.close();
            }
            catch(Throwable t) { }
        }

        synchronized(this)
        {
            cancel = true;
            notifyAll();
        }
    }

    private void checkStubConnects(SimpleInterface stub) throws TestFailed
    {
        connected = false;

        try
        {
            stub.testMethod();
            throw new TestFailed("stub connection to non-skeleton did not " +
                                 "cause an exception");
        }
        catch(RMIException e) { }
        catch(Throwable t)
        {
            throw new TestFailed("exception when attempting to connect to " +
                                 "server", t);
        }

        synchronized(this)
        {
            while(!connected && !cancel)
            {
                try
                {
                    wait();
                }
                catch(InterruptedException e) { }
            }
        }
    }

    private class ServerThread implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                while(true)
                {
                    Socket  connection_socket = listen_socket.accept();

                    synchronized(StubTest.this)
                    {
                        connected = true;
                        StubTest.this.notifyAll();
                    }

                    try
                    {
                        connection_socket.close();
                    }
                    catch(Throwable t) { }
                }
            }
            catch(Throwable t)
            {
                failure(new TestFailed("caught an exception while listening " +
                                       "for connections", t));
            }
        }
    }
}
