package conformance.rmi;

import test.*;
import rmi.*;
import java.net.InetSocketAddress;

public class SecurityTest extends Test
{
    public static final String  notice =
        "checking that only interface methods can be called remotely";

    private InetSocketAddress           address =
        new InetSocketAddress(TestConstants.PORT);
    private SecurityTestSkeleton        skeleton = null;
    private boolean                     started = false;
    private ParallelInterface           stub;

    @Override
    protected void initialize() throws TestFailed
    {
        task("creating skeleton");

        try
        {
            skeleton = new SecurityTestSkeleton();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create skeleton", t);
        }

        task("creating stub");

        try
        {
            stub = Stub.create(ParallelInterface.class, address);
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
    protected void perform() throws TestFailed
    {
        task("calling method declared in parallel remote interface");

        try
        {
            stub.testMethodTwo();
            throw new TestFailed("skeleton permitted call to method not " +
                                 "declared in the interface for which it was " +
                                 "created");
        }
        catch(RMIException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when calling method " +
                                 "not declared in the interface for which " +
                                 "the skeleton was created", t);
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

    public interface MainInterface
    {
        public int testMethodOne() throws RMIException;
    }

    public interface ParallelInterface
    {
        public int testMethodTwo() throws RMIException;
    }

    private static class SecurityTestServer
        implements MainInterface, ParallelInterface
    {
        @Override
        public int testMethodOne()
        {
            return 3;
        }

        @Override
        public int testMethodTwo()
        {
            return 4;
        }
    }

    private class SecurityTestSkeleton extends TestSkeleton<MainInterface>
    {
        SecurityTestSkeleton()
        {
            super(MainInterface.class, new SecurityTestServer(), address,
                  SecurityTest.this);
        }

        @Override
        protected void service_error(RMIException e)
        {
        }
    }
}
