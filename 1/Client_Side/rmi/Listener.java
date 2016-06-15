//Server side program for thread handling
package rmi;

import java.net.ServerSocket;
import java.net.Socket;

public class Listener<T> extends Thread
{
    private ServerSocket sock;
    private T server;
    private Class<T> c;
	private Skeleton<T> skeleton;

	public Listener(ServerSocket sock, T server, Class<T> c, Skeleton<T> skeleton)
	{
		this.sock = sock;
        this.server = server;
        this.c = c;
		this.skeleton = skeleton;
	}

	public void run()
	{		
        try
        {
            while(true)
            {
                Socket s = sock.accept();
                ThreadRunnable thread = new ThreadRunnable(s, server, c, skeleton);
                thread.start();
            }
        }
        catch(Exception e)
        {
			//System.out.println("Server side: Listener Exception");
        }
	}
}