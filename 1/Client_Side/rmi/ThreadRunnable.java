//File for Server side marshalling, unmarshalling, interface checking and running threads

package rmi;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class ThreadRunnable<T> extends Thread
{
    private Socket s;
    private T server;
    private Class<T> c;
	private Skeleton<T> skeleton;

	public ThreadRunnable(Socket s, T server, Class<T> c, Skeleton<T> skeleton)
	{
		this.s = s;
        this.server = server;
        this.c = c;
		this.skeleton = skeleton;
	}
	
	// Method for Checking validity of interface
	private Boolean isValidInterface(Class<T> c,String str)
	{
		Class[] parentInterfaces = c.getInterfaces();

		if (str.equals(c.getName()))
			return true;
		for (Class c1 : parentInterfaces) {
			if (str.equals(c1.getName()))
				return true;
		}
		return false;
	}	
	
	public void run()
	{
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		
		try
		{			
			oos = new ObjectOutputStream(s.getOutputStream());								
			oos.flush();
			
			ois = new ObjectInputStream(s.getInputStream());
			
			String interfaceName = (String)ois.readObject();
			String methodName = (String)ois.readObject();
			Class[] parameterTypes = (Class[])ois.readObject();
			Object[] arguments = (Object[])ois.readObject();
			
			if(isValidInterface(c, interfaceName) == false)
			{
				oos.writeObject(false);
				oos.writeObject(new RMIException("Server side: Interface checking"));
				return;
			}
			
			//Checking heirarchy of interfaces for methods
			boolean flag = false;
			Object result = null;
			
			Method[] m = c.getDeclaredMethods();
			
			for(Method k : m)
			{
				if(k.getName().equals(methodName) && k.getParameterTypes().length == parameterTypes.length)
				{
					flag = true;
					result = k.invoke(server, arguments);
					oos.writeObject(true);
					oos.writeObject(result);
					break;
				}
			}
			
			if(flag == false)
			{
				Class<?>[] parents = c.getInterfaces();
				
				for(Class j: parents)
				{
					Method[] pm = j.getDeclaredMethods();
					
					for(Method k : pm)
					{
						if(k.getName().equals(methodName))
						{
							result =  k.invoke(server, arguments);
							oos.writeObject(true);
							oos.writeObject(result);
							return;
						}
					}
				}
				
				oos.writeObject(false);
				oos.writeObject(new RMIException("Interface"));
			}				
		}
		catch(InvocationTargetException ite)
		{	
			try
			{
				oos.writeObject(false);
				oos.writeObject(ite.getCause());
				//System.out.println("Server Side: InvocationTargetException in ThreadRunnable");
			}
			catch(IOException j)
			{
				//System.out.println("Server side : IO Exception");
			}
		}
		catch(Exception e)
		{
			//System.out.println("Server side: Exception from server thread");
			
			try
			{
				
				oos.writeObject(false);
				oos.writeObject(e);
				//System.out.println("Server side: General exception");
			}
			catch(IOException j)
			{
				skeleton.service_error(new RMIException("Server Side: IO Exception"));
				//System.out.println("Server side : IO Exception");
			}
		}
	}
}
