package conformance.rmi;

import test.*;
import rmi.*;
import java.net.*;

public class EqualsTest extends Test
{
    public static final String  notice = "checking stub equals method";

    private final String    host1 = "1.2.3.4";
    private final String    host2 = "2.3.4.5";

    @Override
    protected void perform() throws TestFailed
    {
        SimpleInterface     stub1, stub2, stub3, stub4, stub5;
        OtherInterface      stub6;

        task("creating stubs");

        try
        {
            stub1 = makeStub(host1, TestConstants.PORT + 0, true);
            stub2 = makeStub(host1, TestConstants.PORT + 0, true);
            stub3 = makeStub(host1, TestConstants.PORT + 0, false);
            stub4 = makeStub(host1, TestConstants.PORT + 1, true);
            stub5 = makeStub(host2, TestConstants.PORT + 0, true);
            stub6 = makeOtherStub(host1, TestConstants.PORT + 0, true);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub", t);
        }

        task("checking correctness of equality");

        try
        {
            if(stub1.equals(null))
                throw new TestFailed("stub reported equal to null");
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "to null", t);
        }

        try
        {
            if(!stub1.equals(stub1))
                throw new TestFailed("stub not equal to itself");
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "to itself", t);
        }

        try
        {
            if(!stub1.equals(stub2))
                throw new TestFailed("stub not equal to identical stub");
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "to identical stub", t);
        }

        try
        {
            if(!stub1.equals(stub3))
            {
                throw new TestFailed("stub not equal to identical stub " +
                                     "created from skeleton");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "to identical stub created from skeleton", t);
        }

        try
        {
            if(stub1.equals(stub4))
            {
                throw new TestFailed("stub reported equal to stub with " +
                                     "different port number");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "to stub with different port number", t);
        }

        try
        {
            if(stub1.equals(stub5))
            {
                throw new TestFailed("stub reported equal to stub with " +
                                     "different hostname");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "to stub with different hostname", t);
        }

        try
        {
            if(stub1.equals(stub6))
            {
                throw new TestFailed("stub reported equal to stub with " +
                                     "different interface type");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "to stub with different interface type", t);
        }

        task("checking whether equals implemented in terms of toString");

        FakeStub            fake_stub = new FakeStub(stub1);

        try
        {
            if(stub1.equals(fake_stub))
            {
                throw new TestFailed("stub reported equal to different " +
                                     "object with same string representation");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "to different object with same string " +
                                 "representation", t);
        }

        task();
    }

    private SimpleInterface makeStub(String host, int port, boolean explicit)
        throws UnknownHostException
    {
        InetSocketAddress   address = new InetSocketAddress(host, port);

        if(explicit)
            return Stub.create(SimpleInterface.class, address);
        else
        {
            Skeleton<SimpleInterface>   dummy_skeleton =
                new Skeleton<SimpleInterface>(SimpleInterface.class,
                                              new SimpleServer(), address);
            return Stub.create(SimpleInterface.class, dummy_skeleton);
        }
    }

    public OtherInterface makeOtherStub(String host, int port, boolean explicit)
        throws UnknownHostException
    {
        InetSocketAddress   address = new InetSocketAddress(host, port);

        if(explicit)
            return Stub.create(OtherInterface.class, address);
        else
        {
            Skeleton<OtherInterface>    dummy_skeleton =
                new Skeleton<OtherInterface>(OtherInterface.class,
                                             new OtherServer(), address);
            return Stub.create(OtherInterface.class, dummy_skeleton);
        }
    }

    public interface OtherInterface
    {
        public void testMethod() throws RMIException;
    }

    private class OtherServer implements OtherInterface
    {
        @Override
        public void testMethod() throws RMIException
        {
        }
    }

    private class FakeStub
    {
        private final SimpleInterface   real_stub;

        FakeStub(SimpleInterface real_stub)
        {
            this.real_stub = real_stub;
        }

        @Override
        public String toString()
        {
            return real_stub.toString();
        }
    }
}
