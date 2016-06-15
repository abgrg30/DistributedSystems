//Class acting as our own Invocation Handler and the class which also takes care of marshalling, unmarshalling and invocation of methods(local or remote)

package rmi;

import java.lang.reflect.Proxy;
import java.lang.reflect.*;
import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;

public class MyInvocationHandler implements InvocationHandler, Serializable
{
	private InetAddress inetAddress;
	private Class c;
	private Integer port;	
	
	public MyInvocationHandler(Class c, InetAddress inetAddress, Integer port)
	{
		this.c = c;
		this.inetAddress = inetAddress;
		this.port = port;
	}
	public MyInvocationHandler(Class c, Integer port)
	{
		this.c = c;
		this.port = port;
		this.inetAddress = null;
	}
	
	
	public InetAddress getAddress()
	{
		return inetAddress;
	}
	
	public Integer getPort()
	{
		return port;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		try
        {
			if(RMIException.isRMIMethod(method) == false)
			{
				//System.out.println("Local Method Executing");
				return localFunctionHandler(proxy, method, args);
			}
	        else
			{
				//System.out.println("Remote Method Executing");
				return useClient(proxy, method, args); 
			}
	    }
        catch (Exception e) 
        {
			//System.out.println("Client Side: Invoke method Exception");
			throw e;
	    }
	}
	
	private Object localFunctionHandler(Object proxy, Method method, Object[] args) throws Throwable
	{
		try
		{			
			if(method.getName().equals("equals"))
			{
				return equals(proxy, method, args);
			}
			
			if(method.getName().equals("hashCode"))
			{			
				return hashCode(proxy, method, args);
			}
			
			if(method.getName().equals("toString"))
			{			
				return toString(proxy, method, args);
			}
			
			return method.invoke(this, args);
		}
		catch(Exception e)
		{
			//System.out.println("Client Side: LocalFunctionHandler Exception");
			throw e;
		}
	}
	
	private String toString(Object proxy, Method method, Object[] args) throws Throwable
	{
		try
		{
			String result = null;
			
			result += c.getSimpleName();
			
			result += " ";
			
			result += inetAddress.getHostName();
			
			result += " ";
			
			result += Integer.toString(port);
			
			return result;
		}
		catch(Exception e)
		{
			//System.out.println("Client Side: toString Exception");
			throw e;
		}	
	}
	
	private Integer hashCode(Object proxy, Method method, Object[] args)
	{
		try
		{			
			InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
			
			if((invocationHandler instanceof MyInvocationHandler) == false)
			{
				throw new IllegalStateException();
			}
			
			MyInvocationHandler dummy = (MyInvocationHandler)invocationHandler;
			
			return dummy.getAddress().hashCode() + dummy.getPort().hashCode() + proxy.getClass().hashCode();
		}
		catch(Exception e)
		{
			//System.out.println("HashCode Exception");
			throw e;
		}		
	}
	
	private boolean equals(Object proxy, Method method, Object[] args)
	{
		try
		{			
			if(args.length != 1)
			{
				throw new IllegalStateException();
			}

			if(args[0] == null)
			{				
				return false;
			}
			
			if(Proxy.isProxyClass(args[0].getClass()) == false)
			{
				throw new IllegalStateException();
			}
			
			if(proxy.getClass().equals(args[0].getClass()) == false)
			{
				return false;
			}		
		
			InvocationHandler invocationHandler = Proxy.getInvocationHandler(args[0]);
			
			if((invocationHandler instanceof MyInvocationHandler)==false)
			{
				throw new IllegalStateException();
			}
			
			MyInvocationHandler dummy = (MyInvocationHandler)invocationHandler;
			
			if(!(this.getAddress().equals(dummy.getAddress()) && this.getPort().equals(dummy.getPort())))
			{
				return false;
			}
			
			return true;
		}
		catch(Exception e)
		{
			//System.out.println("Equals Exception");
			return false;
		}			
	}
	
	public Object useClient(Object proxy, Method method, Object[] args) throws Throwable
	{
		Socket clientSocket = null;
		boolean isServerAlive = true;
		Object result = null;
		
		try
		{			
			clientSocket = new Socket(inetAddress, port);
			
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			
			oos.flush();
			
			oos.writeObject(c.getName());
			oos.writeObject(method.getName());			
			oos.writeObject(method.getParameterTypes());			
			oos.writeObject(args);
			
			ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());							
			
			isServerAlive = (boolean)ois.readObject();

			result = ois.readObject();			
			clientSocket.close();
		}
		catch(Exception e)
		{
			//System.out.println("useClient Exception");
			throw new RMIException("RMI");
		}
		finally
		{
			if(clientSocket != null && clientSocket.isClosed()==false)
			{
				clientSocket.close();
			}
		}
		
        if(isServerAlive == false) 
		{
            throw (Throwable)result;
        }

        return result;		
	}	
}