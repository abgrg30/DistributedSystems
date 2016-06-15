package conformance.rmi;

import test.*;
import rmi.*;
import java.net.*;

public class HashCodeTest extends Test
{
    public static final String  notice = "checking stub hashCode method";

    @Override
    protected void perform() throws TestFailed
    {
        SimpleInterface stub1, stub2, stub3, stub4, stub5, stub6, stub7, stub8;

        task("creating stubs");

        try
        {
            stub1 = makeStub(TestConstants.PORT + 0);
            stub2 = makeStub(TestConstants.PORT + 0);
            stub3 = makeStub(TestConstants.PORT + 1);
            stub4 = makeStub(TestConstants.PORT + 1);
            stub5 = makeStub(TestConstants.PORT + 2);
            stub6 = makeStub(TestConstants.PORT + 3);
            stub7 = makeStub(TestConstants.PORT + 4);
            stub8 = makeStub(TestConstants.PORT + 5);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub", t);
        }

        task("checking that equal stubs have equal hash codes");

        try
        {
            if((stub1.hashCode() != stub2.hashCode()) ||
               (stub3.hashCode() != stub4.hashCode()))
            {
                throw new TestFailed("equal stubs have different hash codes");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "hash codes", t);
        }

        task("checking that unequal stubs can have different hash codes");

        try
        {
            if((stub1.hashCode() == stub3.hashCode()) &&
               (stub1.hashCode() == stub5.hashCode()) &&
               (stub1.hashCode() == stub6.hashCode()) &&
               (stub1.hashCode() == stub7.hashCode()) &&
               (stub1.hashCode() == stub8.hashCode()))
            {
                throw new TestFailed("five different stubs have the same " +
                                     "hash codes: this is possible, but very " +
                                     "unlikely");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when comparing stub " +
                                 "hash codes", t);
        }

        task();
    }

    private SimpleInterface makeStub(int port)
        throws UnknownHostException
    {
        return Stub.create(SimpleInterface.class, new InetSocketAddress(port));
    }
}
