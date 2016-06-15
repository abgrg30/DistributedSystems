package conformance.rmi;

import rmi.*;
import test.*;

public class ReturnTest extends BasicTestBase<ReturnTest.ReturnTestInterface>
{
    public static final String  notice = "checking remote method return value";

    private Object  expected_object;
    private int     expected_integer;

    public ReturnTest()
    {
        super(ReturnTestInterface.class);
        setServer(new ReturnTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        Object      object_result;
        int         integer_result;

        task("calling remote method; expecting string to be returned");

        expected_object = "test string";

        try
        {
            object_result = stub.testObjectReturn();
        }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an exception", t);
        }

        if(!expected_object.equals(object_result))
            throw new TestFailed("remote method returned incorrect object");

        task("calling remote method; expecting null to be returned");

        expected_object = null;

        try
        {
            object_result = stub.testObjectReturn();
        }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an exception", t);
        }

        if(object_result != null)
            throw new TestFailed("remote method did not return null");

        task("calling remote method; expecting exception to be returned " +
             "(not raised)");

        expected_object = new TestException();

        try
        {
            object_result = stub.testExceptionReturn();
        }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an exception", t);
        }

        if(!expected_object.equals(object_result))
            throw new TestFailed("remote method returned incorrect object");

        task("calling remote method; expecting integer to be returned");

        expected_integer = 3;

        try
        {
            integer_result = stub.testIntReturn();
        }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an exception", t);
        }

        if(integer_result != expected_integer)
            throw new TestFailed("remote method returned incorrect integer");

        task();
    }

    public interface ReturnTestInterface
    {
        public String testObjectReturn() throws RMIException, TestException;
        public Throwable testExceptionReturn()
            throws RMIException, TestException;
        public int testIntReturn() throws RMIException;
    }

    private class ReturnTestServer implements ReturnTestInterface
    {
        @Override
        public String testObjectReturn() throws TestException
        {
            return (String)expected_object;
        }

        @Override
        public Throwable testExceptionReturn() throws TestException
        {
            return (Throwable)expected_object;
        }

        @Override
        public int testIntReturn()
        {
            return expected_integer;
        }
    }

    private static class TestException extends Exception
    {
        @Override
        public boolean equals(Object other)
        {
            if(other == null)
                return false;

            return other instanceof TestException;
        }
    }
}
