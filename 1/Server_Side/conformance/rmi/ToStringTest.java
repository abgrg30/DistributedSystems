package conformance.rmi;

import test.*;
import rmi.*;
import java.net.InetSocketAddress;

public class ToStringTest extends Test
{
    public static final String  notice = "checking stub toString method";

    private final String    hostname = "1.2.3.4";

    @Override
    protected void perform() throws TestFailed
    {
        InetSocketAddress   address =
            new InetSocketAddress(hostname, TestConstants.PORT);
        SimpleInterface     stub;

        task("creating stub");

        try
        {
            stub = Stub.create(SimpleInterface.class, address);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub", t);
        }

        task("checking that string contains hostname, port, and interface " +
             "type name");

        try
        {
            if(stub.toString().indexOf(SimpleInterface.class.getSimpleName())
               == -1)
            {
                throw new TestFailed("interface name not found in string " +
                                     "representation of stub");
            }

            if(stub.toString().indexOf(hostname) == -1)
            {
                throw new TestFailed("hostname not found in string " +
                                     "representation of stub");
            }

            if(stub.toString().indexOf(Integer.toString(TestConstants.PORT))
               == -1)
            {
                throw new TestFailed("port number not found in string " +
                                     "representation of stub");
            }
        }
        catch(TestFailed e) { throw e; }
        catch(Throwable t)
        {
            throw new TestFailed("unexpected exception while examining " +
                                 "string representation of stub", t);
        }

        task();
    }
}
