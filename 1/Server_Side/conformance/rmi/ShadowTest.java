package conformance.rmi;

import test.*;
import rmi.*;

public class ShadowTest extends BasicTestBase<ShadowTest.ShadowTestInterface>
{
    public static final String  notice = "checking method shadowing";

    private boolean     called = false;

    public ShadowTest()
    {
        super(ShadowTestInterface.class);
        setServer(new ShadowTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("calling remote methods which should be shadowed");

        try
        {
            stub.equals(stub);
            stub.hashCode();
            stub.toString();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when calling " +
                                 "shadowed method", t);
        }

        if(called)
        {
            throw new TestFailed("remote method with same signature as local " +
                                 "method not shadowed by local method");
        }

        task();
    }

    public interface ShadowTestInterface
    {
    }

    private class ShadowTestServer implements ShadowTestInterface
    {
        @Override
        public boolean equals(Object other)
        {
            called = true;
            return false;
        }

        @Override
        public int hashCode()
        {
            called = true;
            return 0;
        }

        @Override
        public String toString()
        {
            called = true;
            return "";
        }
    }
}
