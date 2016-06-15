package conformance.rmi;

import test.*;
import rmi.*;
import java.net.*;

public class RestartTest extends Test
{
    public static final String  notice = "checking skeleton restart";

    private final InetSocketAddress address =
        new InetSocketAddress(TestConstants.PORT);

    @Override
    protected void initialize() throws TestFailed
    {
        checkExplicitAddressSkeletonRestarts();
        checkImplicitAddressSkeletonRestarts();
    }

    private void checkExplicitAddressSkeletonRestarts() throws TestFailed
    {
        RestartTestSkeleton     explicit_skeleton;

        task("checking that the default test port is not already open");

        if(probe())
        {
            throw new TestFailed("skeleton from previous test appears to " +
                                 "still be running");
        }

        task("creating test skeleton with explicit address");

        try
        {
            explicit_skeleton = new RestartTestSkeleton(address);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create skeleton with explicit " +
                                 "address", t);
        }

        task("checking that the skeleton with explicit address is not " +
             "listening before start");

        if(probe())
            throw new TestFailed("skeleton accepts connections before start");

        task("starting test skeleton with explicit address");

        try
        {
            explicit_skeleton.start();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to start skeleton with explicit " +
                                 "address", t);
        }

        task("checking that the skeleton with explicit address is listening " +
             "after start");

        if(!probe())
        {
            throw new TestFailed("skeleton with explicit address refuses " +
                                 "connections after start");
        }

        task("stopping the skeleton with explicit address");

        try
        {
            explicit_skeleton.stop();
        }
        catch(Throwable t)
        {
            throw new TestFailed("exception while stopping skeleton with " +
                                 "explicit address", t);
        }

        task("checking that the skeleton with explicit address is not " +
             "listening after stop");

        if(probe())
        {
            throw new TestFailed("skeleton with explicit address accepts " +
                                 "connections after stop");
        }

        task("waiting for the skeleton with explicit address to stop");

        explicit_skeleton.join();

        // Restart.
        task("restarting test skeleton with explicit address");

        try
        {
            explicit_skeleton.start();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to restart skeleton with explicit " +
                                 "address", t);
        }

        task("checking that the skeleton with explicit address is listening " +
             "after restart");

        if(!probe())
        {
            throw new TestFailed("skeleton with explicit address refuses " +
                                 "connections after restart");
        }

        task("stopping the skeleton with explicit address");

        try
        {
            explicit_skeleton.stop();
        }
        catch(Throwable t)
        {
            throw new TestFailed("exception while stopping skeleton with " +
                                 "explicit address", t);
        }

        task("checking that the skeleton with explicit address is not " +
             "listening after stop");

        if(probe())
        {
            throw new TestFailed("skeleton with explicit address accepts " +
                                 "connections after stop");
        }

        task("waiting for the skeleton with explicit address to stop");

        explicit_skeleton.join();

        task();
    }

    private void checkImplicitAddressSkeletonRestarts() throws TestFailed
    {
        RestartTestSkeleton     implicit_skeleton;
        SimpleInterface         stub;

        task("creating test skeleton with implicit address");

        try
        {
            implicit_skeleton = new RestartTestSkeleton();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create skeleton with implicit " +
                                 "address", t);
        }

        task("starting test skeleton with implicit address");

        try
        {
            implicit_skeleton.start();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to start skeleton with implicit " +
                                 "address", t);
        }

        task("creating test stub from skeleton with implicit address");

        try
        {
            stub = Stub.create(SimpleInterface.class, implicit_skeleton);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub from skeleton with " +
                                 "implicit address", t);
        }

        task("checking that the skeleton with implicit address is listening " +
             "after start");

        if(!stubProbe(stub))
        {
            throw new TestFailed("skeleton with implicit address refuses " +
                                 "connections after start");
        }

        task("stopping the skeleton with implicit address");

        try
        {
            implicit_skeleton.stop();
        }
        catch(Throwable t)
        {
            throw new TestFailed("exception while stopping skeleton with " +
                                 "implicit address", t);
        }

        task("checking that the skeleton with implicit address is not " +
             "listening after stop");

        if(stubProbe(stub))
        {
            throw new TestFailed("skeleton with implicit address accepts " +
                                 "connections after stop");
        }

        task("waiting for the skeleton with implicit address to stop");

        implicit_skeleton.join();

        // Restart.
        task("restarting test skeleton with implicit address");

        try
        {
            implicit_skeleton.start();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to restart skeleton with implicit " +
                                 "address", t);
        }

        task("checking that the skeleton with implicit address is listening " +
             "after restart");

        if(!stubProbe(stub))
        {
            throw new TestFailed("skeleton with implicit address refuses " +
                                 "connections after restart");
        }

        task("stopping the skeleton with implicit address");

        try
        {
            implicit_skeleton.stop();
        }
        catch(Throwable t)
        {
            throw new TestFailed("exception while stopping skeleton with " +
                                 "implicit address", t);
        }

        task("checking that the skeleton with implicit address is not " +
             "listening after stop");

        if(stubProbe(stub))
        {
            throw new TestFailed("skeleton with implicit address accepts " +
                                 "connections after stop");
        }

        task("waiting for the skeleton with implicit address to stop");

        implicit_skeleton.join();

        task();
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

    private boolean stubProbe(SimpleInterface stub)
    {
        try
        {
            stub.testMethod();
        }
        catch(Throwable t)
        {
            return false;
        }

        return true;
    }

    @Override
    protected void perform() throws TestFailed
    {
    }

    private class RestartTestSkeleton extends TestSkeleton<SimpleInterface>
    {
        RestartTestSkeleton()
        {
            super(SimpleInterface.class, new SimpleServer(), RestartTest.this);
        }

        RestartTestSkeleton(InetSocketAddress address)
        {
            super(SimpleInterface.class, new SimpleServer(), address,
                  RestartTest.this);
        }

        @Override
        protected void service_error(RMIException e)
        {
        }
    }
}
