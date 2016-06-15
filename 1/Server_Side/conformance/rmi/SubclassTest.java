package conformance.rmi;

import test.*;
import rmi.*;

public class SubclassTest
    extends BasicTestBase<SubclassTest.SubclassTestInterface>
{
    public static final String  notice = "checking server subclassing";

    public SubclassTest()
    {
        super(SubclassTestInterface.class);
        setServer(new DerivedServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("calling server class methods");

        try
        {
            if((stub.testMethodOne() != 5) ||
               (stub.testMethodThree(3, 4) != 12))
            {
                throw new TestFailed("unexpected result from method call");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when calling " +
                                 "server subclass method", t);
        }

        try
        {
            if(stub.testMethodTwo(6) != 6)
            {
                throw new TestFailed("unexpected result from method call");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception when calling " +
                                 "server superclass method", t);
        }

        task();
    }

    public interface SubclassTestInterface
    {
        public int testMethodOne() throws RMIException;
        public int testMethodTwo(int x) throws RMIException;
        public int testMethodThree(int x, int y) throws RMIException;
    }

    private static abstract class BaseServer implements SubclassTestInterface
    {
        @Override
        public int testMethodTwo(int x)
        {
            return x;
        }

        @Override
        public int testMethodThree(int x, int y)
        {
            return x + y;
        }
    }

    private static class DerivedServer extends BaseServer
    {
        @Override
        public int testMethodOne()
        {
            return 5;
        }

        @Override
        public int testMethodThree(int x, int y)
        {
            return x * y;
        }
    }
}
