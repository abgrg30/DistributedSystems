package conformance.rmi;

import rmi.*;
import test.*;

public class CallTest extends BasicTestBase<CallTest.CallTestInterface>
{
    public static final String  notice = "checking basic remote method call";

    private boolean called = false;

    public CallTest()
    {
        super(CallTestInterface.class);
        setServer(new CallTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("calling remote method");

        try
        {
            stub.testMethod();
        }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an exception", t);
        }

        if(!called)
            throw new TestFailed("remote method not called");

        task();
    }

    public interface CallTestInterface
    {
        public void testMethod() throws RMIException;
    }

    private class CallTestServer implements CallTestInterface
    {
        @Override
        public void testMethod()
        {
            called = true;
        }
    }
}
