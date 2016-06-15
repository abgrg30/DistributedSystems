package rmi;

//Remote Interface for PingPongServer
public interface RemoteInterface
{
	public String ping(int idNumber) throws RMIException;
}