package conformance.rmi;

import test.*;
import rmi.*;
import java.net.InetSocketAddress;

public class InheritanceTest
    extends BasicTestBase<InheritanceTest.InheritanceTestInterface>
{
    public static final String  notice =
        "checking remote interface inheritance";

    public InheritanceTest()
    {
        super(InheritanceTestInterface.class);
        setServer(new InheritanceTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("checking that remote method checking extends to superinterfaces");

        try
        {
            Stub.create(NonRemoteDerivedInterface.class,
                        new InetSocketAddress(TestConstants.PORT));
            throw new TestFailed("stub factory method accepted interface " +
                                 "with non-remote superinterface");
        }
        catch(Error e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw unexpected " +
                                 "exception when given interface with " +
                                 "non-remote superinterface", t);
        }

        task("calling inherited remote method");

        try
        {
            if(stub.testMethod() != 4)
                throw new TestFailed("unexpected result from method call");
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when calling " +
                                 "inherited interface method", t);
        }

        task();
    }

    public interface BaseTestInterface
    {
        public int testMethod() throws RMIException;
    }

    public interface InheritanceTestInterface extends BaseTestInterface
    {
    }

    private class InheritanceTestServer implements InheritanceTestInterface
    {
        @Override
        public int testMethod()
        {
            return 4;
        }
    }

    public interface NonRemoteBaseInterface
    {
        public int testMethod();
    }

    public interface NonRemoteDerivedInterface extends NonRemoteBaseInterface
    {
    }
}
