package conformance.rmi;

import test.*;
import rmi.*;
import java.net.InetSocketAddress;

class TestSkeleton<T> extends Skeleton<T>
{
    protected final Test    test;
    private boolean         stopped = false;
    private Throwable       stop_cause;

    TestSkeleton(Class<T> remote_interface, T server, Test test)
    {
        super(remote_interface, server);

        this.test = test;
    }

    TestSkeleton(Class<T> remote_interface, T server, InetSocketAddress address,
                 Test test)
    {
        super(remote_interface, server, address);

        this.test = test;
    }

    @Override
    protected synchronized void stopped(Throwable cause)
    {
        stopped = true;
        stop_cause = cause;
        notifyAll();
    }

    @Override
    protected boolean listen_error(Exception e)
    {
        test.failure(new TestFailed("error in skeleton listening thread", e));

        return false;
    }

    @Override
    protected void service_error(RMIException e)
    {
        test.failure(new TestFailed("error in skeleton service thread", e));
    }

    synchronized Throwable join()
    {
        while(!stopped)
        {
            try
            {
                wait();
            }
            catch(InterruptedException e) { }
        }

        return stop_cause;
    }
}
