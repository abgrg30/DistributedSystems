package conformance.rmi;

import rmi.*;
import test.*;
import java.io.FileNotFoundException;

public class CompleteCallTest
    extends BasicTestBase<CompleteCallTest.CompleteCallTestInterface>
{
    public static final String  notice =
        "checking argument passing, return, and exceptions at once";

    public CompleteCallTest()
    {
        super(CompleteCallTestInterface.class);
        setServer(new CompleteCallTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("calling remote method; expecting regular return");

        boolean     result;

        try
        {
            result = stub.testMethod(false);
        }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an exception", t);
        }

        if(result != true)
            throw new TestFailed("remote method returned incorrect result");

        task("calling remote method; expecting an exception");

        try
        {
            stub.testMethod(true);
            throw new TestFailed("remote method did not throw an exception");
        }
        catch(FileNotFoundException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("remote method call threw an unexpected " +
                                 "exception", t);
        }

        task();
    }

    public interface CompleteCallTestInterface
    {
        public boolean testMethod(boolean throw_exception)
            throws RMIException, FileNotFoundException;
    }

    private static class CompleteCallTestServer
        implements CompleteCallTestInterface
    {
        @Override
        public boolean testMethod(boolean throw_exception)
            throws FileNotFoundException
        {
            if(throw_exception)
                throw new FileNotFoundException();
            else
                return true;
        }
    }
}
