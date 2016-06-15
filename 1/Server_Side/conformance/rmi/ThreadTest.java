package conformance.rmi;

import rmi.*;
import test.*;

public class ThreadTest extends BasicTestBase<ThreadTest.ThreadTestInterface>
{
    public static final String  notice = "checking skeleton multithreading";

    private boolean     wake = false;
    private boolean     sleeping = true;
    private boolean     cancel = false;

    public ThreadTest()
    {
        super(ThreadTestInterface.class);
        setServer(new ThreadTestServer());
    }

    @Override
    protected void perform() throws TestFailed
    {
        task("arranging thread rendezvous in the server");

        new Thread(new SecondThread()).start();

        try
        {
            stub.rendezvous();
        }
        catch(Throwable t)
        {
            throw new TestFailed("unable to rendezvous in the first thread", t);
        }

        task();
    }

    @Override
    protected void clean()
    {
        synchronized(this)
        {
            cancel = true;
            notifyAll();
        }

        super.clean();
    }

    private class SecondThread implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                stub.rendezvous();
            }
            catch(Throwable t)
            {
                failure(new TestFailed("unable to rendezvous in second " +
                                       "thread", t));
            }
        }
    }

    public interface ThreadTestInterface
    {
        public void rendezvous() throws RMIException;
    }

    private class ThreadTestServer implements ThreadTestInterface
    {
        @Override
        public void rendezvous()
        {
            synchronized(ThreadTest.this)
            {
                if(!wake)
                {
                    wake = true;

                    while(sleeping && !cancel)
                    {
                        try
                        {
                            ThreadTest.this.wait();
                        }
                        catch(InterruptedException e) { }
                    }
                }
                else
                {
                    sleeping = false;
                    ThreadTest.this.notifyAll();
                }
            }
        }
    }
}
