package conformance.rmi;

import rmi.*;
import test.*;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;

public class ImplicitStubCallTest
    extends BasicTestBase<ImplicitStubCallTest.ImplicitStubCallTestInterface>
{
    public static final String  notice =
        "checking method calls on stub created from a skeleton";

    public ImplicitStubCallTest()
    {
        super(ImplicitStubCallTestInterface.class);
        setServer(new ImplicitStubCallTestServer());
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

    @Override
    protected void initialize() throws TestFailed
    {
        InetSocketAddress       address =
            new InetSocketAddress(TestConstants.PORT);

        task("creating skeleton");

        try
        {
            skeleton = new TestSkeleton<ImplicitStubCallTestInterface>(
                                    remote_interface, server, address, this);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create skeleton", t);
        }

        task("creating stub");

        try
        {
            stub = Stub.create(remote_interface, skeleton);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub", t);
        }

        task("starting skeleton");

        try
        {
            skeleton.start();
            started = true;
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to start skeleton", t);
        }

        task();
    }

    public interface ImplicitStubCallTestInterface
    {
        public boolean testMethod(boolean throw_exception)
            throws RMIException, FileNotFoundException;
    }

    private static class ImplicitStubCallTestServer
        implements ImplicitStubCallTestInterface
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
