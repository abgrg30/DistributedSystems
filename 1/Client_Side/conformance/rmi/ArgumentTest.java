package conformance.rmi;

import rmi.*;
import test.*;

public class ArgumentTest
    extends BasicTestBase<ArgumentTest.ArgumentTestInterface>
{
    public static final String  notice =
        "checking remote method argument passing";

    private boolean         failed = false;
    private final String    expected_object = "test string";
    private final int       expected_integer = 3;

    public ArgumentTest()
    {
        super(ArgumentTestInterface.class);
        setServer(new ArgumentTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("calling remote method with object argument");

        try
        {
            stub.testObjectArgument(expected_object);
        }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an exception", t);
        }

        if(failed)
            throw new TestFailed("argument did not match expected string");

        task("calling remote method with integer argument");

        try
        {
            stub.testIntArgument(expected_integer);
        }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an exception", t);
        }

        if(failed)
            throw new TestFailed("argument did not match expected integer");

        task();
    }

    public interface ArgumentTestInterface
    {
        public void testObjectArgument(String object) throws RMIException;
        public void testIntArgument(int integer) throws RMIException;
    }

    private class ArgumentTestServer implements ArgumentTestInterface
    {
        @Override
        public void testObjectArgument(String object)
        {
            if(!object.equals(expected_object))
                failed = true;
        }

        @Override
        public void testIntArgument(int integer)
        {
            if(integer != expected_integer)
                failed = true;
        }
    }
}
