package conformance.rmi;

import test.*;
import rmi.*;
import java.net.InetSocketAddress;

public class NullTest extends Test
{
    public static final String  notice =
        "checking that skeleton constructors and stub factory reject null";

    @Override
    protected void perform() throws TestFailed
    {
        checkSkeletonConstructors();
        checkStubCreateMethods();
    }

    private void checkSkeletonConstructors() throws TestFailed
    {
        InetSocketAddress   dummy_address =
            new InetSocketAddress(TestConstants.PORT);
        SimpleServer        dummy_server = new SimpleServer();

        task("checking that skeleton constructors reject null class object " +
             "arguments");

        try
        {
            new Skeleton<SimpleInterface>(null, dummy_server);
            throw new TestFailed("skeleton constructor accepted null for " +
                                 "class object argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor threw an unexpected " +
                                 "exception when given null for class object " +
                                 "argument", t);
        }

        try
        {
            new Skeleton<SimpleInterface>(null, dummy_server, dummy_address);
            throw new TestFailed("skeleton constructor accepted null for " +
                                 "class object argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor threw an unexpected " +
                                 "exception when given null for class object " +
                                 "argument", t);
        }

        task("checking that skeleton constructors reject null server object " +
             "arguments");

        try
        {
            new Skeleton<SimpleInterface>(SimpleInterface.class, null);
            throw new TestFailed("skeleton constructor accepted null for " +
                                 "server object argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor threw an unexpected " +
                                 "exception when given null for server " +
                                 "object argument", t);
        }

        try
        {
            new Skeleton<SimpleInterface>(SimpleInterface.class, null,
                                            dummy_address);
            throw new TestFailed("skeleton constructor accepted null for " +
                                 "server object argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor threw an unexpected " +
                                 "exception when given null for server " +
                                 "object argument", t);
        }

        task("checking that skeleton constructor does accept null for the " +
             "address argument");

        try
        {
            new Skeleton<SimpleInterface>(SimpleInterface.class, dummy_server,
                                          null);
        }
        catch(Throwable t)
        {
            throw new TestFailed("skeleton constructor rejected null for " +
                                 "address argument", t);
        }

        task();
    }

    private void checkStubCreateMethods() throws TestFailed
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

        task("checking that stub factory methods reject null class object " +
             "arguments");

        try
        {
            Stub.create(null, dummy_address);
            throw new TestFailed("stub factory method accepted null for " +
                                 "class object argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given null for class object " +
                                 "argument", t);
        }

        try
        {
            Stub.create(null, dummy_skeleton);
            throw new TestFailed("stub factory method accepted null for " +
                                 "class object argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given null for class object " +
                                 "argument", t);
        }

        try
        {
            Stub.create(null, dummy_skeleton, "127.0.0.1");
            throw new TestFailed("stub factory method accepted null for " +
                                 "class object argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given null for class object " +
                                 "argument", t);
        }

        task("checking that stub factory methods reject null address, " +
             "skeleton, and hostname arguments");

        try
        {
            Stub.create(SimpleInterface.class, (InetSocketAddress)null);
            throw new TestFailed("stub factory method accepted null for " +
                                 "address argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given null for address " +
                                 "argument", t);
        }

        try
        {
            Stub.create(SimpleInterface.class, (Skeleton<SimpleInterface>)null);
            throw new TestFailed("stub factory method accepted null for " +
                                 "skeleton argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given null for skeleton " +
                                 "argument", t);
        }

        try
        {
            Stub.create(SimpleInterface.class, null, "127.0.0.1");
            throw new TestFailed("stub factory method accepted null for " +
                                 "skeleton argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given null for skeleton " +
                                 "argument", t);
        }

        try
        {
            Stub.create(SimpleInterface.class, dummy_skeleton, null);
            throw new TestFailed("stub factory method accepted null for " +
                                 "hostname argument");
        }
        catch(NullPointerException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when given null for hostname " +
                                 "argument", t);
        }

        task();
    }
}
