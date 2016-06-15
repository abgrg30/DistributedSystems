package conformance.rmi;

import test.*;
import rmi.*;
import java.net.*;

public class ServiceErrorTest extends Test
{
    public static final String  notice =
        "checking that skeleton calls service_error";
    public static final Class[] prerequisites = {CallTest.class};

    private ServiceErrorTestSkeleton    skeleton = null;
    private final InetSocketAddress     address =
        new InetSocketAddress(TestConstants.PORT);
    private boolean                     error_occurred = false;
    private boolean                     cancel = false;
    private boolean                     started = false;

    @Override
    protected void initialize() throws TestFailed
    {
        task("creating test skeleton");

        try
        {
            skeleton = new ServiceErrorTestSkeleton();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create test skeleton", t);
        }

        task("starting test skeleton");

        try
        {
            skeleton.start();
            started = true;
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to start test skeleton", t);
        }

        task();
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("contacting test skeleton to cause an exception in the service " +
             "thread");

        probe();

        task("waiting for service_error to be called by the skeleton");

        synchronized(this)
        {
            while(!error_occurred && !cancel)
            {
                try
                {
                    wait();
                }
                catch(InterruptedException e) { }
            }
        }

        task();
    }

    @Override
    protected void clean()
    {
        if((skeleton != null) && started)
        {
            skeleton.stop();
            skeleton.join();
        }

        synchronized(this)
        {
            cancel = true;
            notifyAll();
        }
    }

    private void probe()
    {
        Socket      socket = new Socket();

        try
        {
            socket.connect(address);
        }
        catch(Throwable t)
        {
            return;
        }

        try
        {
            socket.close();
        }
        catch(Throwable t) { }
    }

    private class ServiceErrorTestSkeleton
        extends TestSkeleton<SimpleInterface>
    {
        ServiceErrorTestSkeleton()
        {
            super(SimpleInterface.class, new SimpleServer(), address,
                  ServiceErrorTest.this);
        }

        @Override
        protected void service_error(RMIException e)
        {
            synchronized(ServiceErrorTest.this)
            {
                if(error_occurred)
                {
                    test.failure(new TestFailed("only one call to " +
                                                "service_error is expected"));
                }

                error_occurred = true;
                ServiceErrorTest.this.notifyAll();
            }
        }
    }
}
