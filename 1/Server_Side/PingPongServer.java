//PingPongServer file with ping method

import rmi.*;

public class PingPongServer implements RemoteInterface
{	
	public String ping(int idNumber) throws RMIException
	{
		return "Pong " + idNumber;
	}
}