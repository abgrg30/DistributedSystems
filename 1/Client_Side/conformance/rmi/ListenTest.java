package conformance.rmi;

import test.*;
import rmi.*;
import java.net.*;

public class ListenTest extends Test
{
    public static final String  notice = "checking skeleton listening";

    private ListenTestSkeleton      skeleton = null;
    private final InetSocketAddress address =
        new InetSocketAddress(TestConstants.PORT);
    private boolean                 started = false;
    private boolean                 stopped = false;

    @Override
    protected void initialize() throws TestFailed
    {
        task("checking that the test port is not already open");

        if(probe())
        {
            throw new TestFailed("skeleton from previous test appears to " +
                                 "still be running");
        }

        task("creating test skeleton");

        try
        {
            skeleton = new ListenTestSkeleton();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create skeleton", t);
        }

        task("checking that the skeleton is not listening before start");

        if(probe())
            throw new TestFailed("skeleton accepts connections before start");

        task("starting test skeleton");

        try
        {
            skeleton.start();
            started = true;
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to start skeleton", t);
        }

        task();
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("checking that the skeleton is listening after start");

        if(!probe())
            throw new TestFailed("skeleton refuses connections after start");

        task("stopping the skeleton");

        try
        {
            stop();
        }
        catch(Throwable t)
        {
            throw new TestFailed("exception while stopping skeleton", t);
        }

        task("checking that the skeleton is not listening after stop");

        if(probe())
            throw new TestFailed("skeleton accepts connections after stop");

        task();
    }

    @Override
    protected void clean()
    {
        if((skeleton != null) && started)
        {
            stop();
            skeleton.join();
        }
    }

    private void stop()
    {
        synchronized(this)
        {
            if(!stopped)
            {
                skeleton.stop();
                stopped = true;
            }
        }
    }

    private boolean probe()
    {
        Socket      socket = new Socket();

        try
        {
            socket.connect(address);
        }
        catch(Throwable t)
        {
            return false;
        }

        try
        {
            socket.close();
        }
        catch(Throwable t) { }

        return true;
    }

    private class ListenTestSkeleton extends TestSkeleton<SimpleInterface>
    {
        ListenTestSkeleton()
        {
            super(SimpleInterface.class, new SimpleServer(), address,
                  ListenTest.this);
        }

        @Override
        protected void service_error(RMIException e)
        {
        }
    }
}
