package conformance.rmi;

import test.*;
import rmi.*;
import java.net.InetSocketAddress;

abstract class BasicTestBase<T> extends Test
{
    protected final Class<T>    remote_interface;
    protected T                 server;

    protected TestSkeleton<T>   skeleton = null;
    protected boolean           started = false;
    protected T                 stub;

    protected BasicTestBase(Class<T> remote_interface)
    {
        this.remote_interface = remote_interface;
    }

    protected void setServer(T server)
    {
        this.server = server;
    }

    @Override
    protected void initialize() throws TestFailed
    {
        InetSocketAddress       address =
            new InetSocketAddress(TestConstants.PORT);

        task("creating skeleton");

        try
        {
            skeleton =
                new TestSkeleton<T>(remote_interface, server, address, this);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create skeleton", t);
        }

        task("creating stub");

        try
        {
            stub = Stub.create(remote_interface, address);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub", t);
        }

        task("starting skeleton");

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
    protected void clean()
    {
        if((skeleton != null) && started)
        {
            skeleton.stop();
            skeleton.join();
        }
    }
}
