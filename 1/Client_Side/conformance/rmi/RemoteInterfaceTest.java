package conformance.rmi;

import test.*;
import rmi.*;
import java.net.InetSocketAddress;

public class RemoteInterfaceTest extends Test
{
    public static final String  notice =
        "checking that skeleton constructors and stub factory reject bad " +
        "interfaces";

    @Override
    protected void perform() throws TestFailed
    {
        checkClassRejected();
        checkNonRemoteInterfaceRejected();
    }

    @SuppressWarnings("unchecked")
    private void checkClassRejected() throws TestFailed
    {
        Skeleton<SimpleInterface>   dummy_skeleton;
        SimpleServer                dummy_server = new SimpleServer();
        InetSocketAddress           dummy_address =
            new InetSocketAddress(TestConstants.PORT);

        task("creating dummy skeleton");

        try
        {
            dummy_skeleton =
                new Skeleton<SimpleInterface>(SimpleInterface.class,
                                              dummy_server, dummy_address);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create dummy skeleton", t);
        }

        task("checking that skeleton constructors reject classes");

        try
        {
            new Skeleton<SimpleServer>(SimpleServer.class, dummy_server);
            throw new TestFailed("skeleton constructor accepted a class");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor threw an unexpected " +
                                 "exception when given a class", t);
        }

        try
        {
            new Skeleton<SimpleServer>(SimpleServer.class, dummy_server,
                                       dummy_address);
            throw new TestFailed("skeleton constructor accepted a class");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor threw an unexpected " +
                                 "exception when given a class", t);
        }

        task("checking that stub factory methods reject classes");

        try
        {
            Stub.create(SimpleServer.class, dummy_address);
            throw new TestFailed("stub factory method accepted a class");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given a class", t);
        }

        try
        {
            Stub.create(SimpleServer.class, (Skeleton)dummy_skeleton);
            throw new TestFailed("stub factory method accepted a class");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given a class", t);
        }

        try
        {
            Stub.create(SimpleServer.class, (Skeleton)dummy_skeleton,
                        "127.0.0.1");
            throw new TestFailed("stub factory method accepted a class");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given a class", t);
        }

        task();
    }

    @SuppressWarnings("unchecked")
    private void checkNonRemoteInterfaceRejected() throws TestFailed
    {
        Skeleton<SimpleInterface>   dummy_skeleton;
        SimpleServer                dummy_skeleton_server = new SimpleServer();
        NonRemoteInterfaceServer    dummy_server =
            new NonRemoteInterfaceServer();
        InetSocketAddress           dummy_address =
            new InetSocketAddress(TestConstants.PORT);

        task("creating dummy skeleton");

        try
        {
            dummy_skeleton =
                new Skeleton<SimpleInterface>(SimpleInterface.class,
                                              dummy_skeleton_server,
                                              dummy_address);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create dummy skeleton", t);
        }

        task("checking that skeleton constructors reject non-remote " +
             "interfaces");

        try
        {
            new Skeleton<NonRemoteInterface>(NonRemoteInterface.class,
                                             dummy_server);
            throw new TestFailed("skeleton constructor accepted a non-remote " +
                                 "interface");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor threw an unexpected " +
                                 "exception when given a non-remote interface",
                                 t);
        }

        try
        {
            new Skeleton<NonRemoteInterface>(NonRemoteInterface.class,
                                             dummy_server, dummy_address);
            throw new TestFailed("skeleton constructor accepted a non-remote " +
                                 "interface");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor threw an unexpected " +
                                 "exception when given a non-remote interface",
                                 t);
        }

        task("checking that stub factory methods reject non-remote interfaces");

        try
        {
            Stub.create(NonRemoteInterface.class, dummy_address);
            throw new TestFailed("stub factory method accepted a non-remote " +
                                 "interface");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given a non-remote interface",
                                 t);
        }

        try
        {
            Stub.create(NonRemoteInterface.class, (Skeleton)dummy_skeleton);
            throw new TestFailed("stub factory method accepted a non-remote " +
                                 "interface");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given a non-remote interface",
                                 t);
        }

        try
        {
            Stub.create(NonRemoteInterface.class, (Skeleton)dummy_skeleton,
                        "127.0.0.1");
            throw new TestFailed("stub factory method accepted a non-remote " +
                                 "interface");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given a non-remote interface",
                                 t);
        }

        task();
    }

    public interface NonRemoteInterface
    {
        public void nonRemoteMethod();
    }

    private static class NonRemoteInterfaceServer implements NonRemoteInterface
    {
        public void nonRemoteMethod()
        {
        }
    }
}
