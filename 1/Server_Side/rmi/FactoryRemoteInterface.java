package rmi;

// Remote Interface for PingPongServerFactory
public interface FactoryRemoteInterface
{
	public RemoteInterface makePingServer() throws RMIException;
}