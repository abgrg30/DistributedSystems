package conformance.rmi;

import test.*;
import rmi.*;
import java.net.InetSocketAddress;
import java.io.*;

public class SerializableTest extends Test
{
    public static final String  notice = "checking that stubs are serializable";

    @Override
    protected void perform() throws TestFailed
    {
        SimpleInterface     stub;

        task("creating stub");

        try
        {
            stub = Stub.create(SimpleInterface.class,
                               new InetSocketAddress(TestConstants.PORT));
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create stub", t);
        }

        task("checking that stub is serializable");

        ByteArrayOutputStream   byte_stream = new ByteArrayOutputStream();
        ObjectOutputStream      object_stream;

        try
        {
            object_stream = new ObjectOutputStream(byte_stream);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to create object output stream", t);
        }

        try
        {
            object_stream.writeObject(stub);
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to serialize stub", t);
        }

        task();
    }
}
