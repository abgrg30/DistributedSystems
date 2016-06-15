package conformance.rmi;

import rmi.*;
import test.*;
import java.io.FileNotFoundException;

public class ExceptionTest
    extends BasicTestBase<ExceptionTest.ExceptionTestInterface>
{
    public static final String  notice =
        "checking remote method exception forwarding";

    public ExceptionTest()
    {
        super(ExceptionTestInterface.class);
        setServer(new ExceptionTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("calling remote method");

        try
        {
            stub.testMethod();
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

    public interface ExceptionTestInterface
    {
        public void testMethod() throws RMIException, FileNotFoundException;
    }

    private static class ExceptionTestServer implements ExceptionTestInterface
    {
        @Override
        public void testMethod() throws FileNotFoundException
        {
            throw new FileNotFoundException();
        }
    }
}
