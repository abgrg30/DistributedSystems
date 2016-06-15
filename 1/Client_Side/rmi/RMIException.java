package rmi;

import java.lang.reflect.Method;

/** RMI exceptions. */
public class RMIException extends Exception
{
    /** Creates an <code>RMIException</code> with the given message string. */
    public RMIException(String message)
    {
        super(message);
    }

    /** Creates an <code>RMIException</code> with a message string and the given
        cause. */
    public RMIException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /** Creates an <code>RMIException</code> from the given cause. */
    public RMIException(Throwable cause)
    {
        super(cause);
    }
	
	//Check for method to be RMI method
	public static boolean isRMIMethod(Method method)
	{
		Class<?>[] exceptionTypes =  method.getExceptionTypes();
		
		for(Class j: exceptionTypes)
		{
			if(j.equals(RMIException.class))
			{
				return true;
			}
		}
		
		return false;
	}
	
	//Check for interface to be RMI
	public static boolean isRemoteInterface(Class c)
	{
		if(c.isInterface() == false)
		{
			return false;
		}
		
		Class<?>[] otherInterfaces = c.getInterfaces();
		
		for(Class j: otherInterfaces)
		{
			if(isRemoteInterface(j) == false)
			{
				return false;
			}
		}
		
		Method[] myMethods = c.getDeclaredMethods();
		
		for(Method m: myMethods)
		{
			if(!isRMIMethod(m))
			{
				return false;
			}
		}
		
		return true;		
	}
}
