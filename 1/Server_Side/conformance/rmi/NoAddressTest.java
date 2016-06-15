package conformance.rmi;

import test.*;
import rmi.*;

public class NoAddressTest extends Test
{
    public static final String  notice =
        "checking that stubs can't be made from skeletons with no address";

    @Override
    protected void perform() throws TestFailed
    {
        Skeleton<SimpleInterface>   skeleton;

        task("creating test skeleton");

        try
        {
            skeleton = new Skeleton<SimpleInterface>(SimpleInterface.class,
                                                     new SimpleServer());
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create skeleton", t);
        }

        task("creating stub from test skeleton while skeleton has no address");

        try
        {
            Stub.create(SimpleInterface.class, skeleton);
            throw new TestFailed("stub factory method created a stub from a " +
                                 "skeleton with no assigned address");
        }
        catch(IllegalStateException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when creating a stub from a " +
                                 "skeleton with no assigned address", t);
        }

        try
        {
            Stub.create(SimpleInterface.class, skeleton, "127.0.0.1");
            throw new TestFailed("stub factory method created a stub from a " +
                                 "skeleton with no assigned port");
        }
        catch(IllegalStateException e) { }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("stub factory method threw an unexpected " +
                                 "exception when creating a stub from a " +
                                 "skeleton with no assigned port", t);
        }

        task();
    }
}
