//PingPongServerFactory file for main functionality

import rmi.*;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class PingPongServerFactory implements FactoryRemoteInterface
{	
	private static String url = "server1";
	private static Integer factoryPort = 6000;
	private static Integer serverPort = 4000;
	
	public static void main(String[] args) throws RMIException, UnknownHostException
	{
		//created a new instance of PingPongServerFactory for use in skeleton of PingPongServerFactory
		PingPongServerFactory pingPongServerFactory = new PingPongServerFactory();
		// gave this skeleton an instance of PingPongServerFactory created above and since it is a skeleton, it contains listener,
		// threadrunnable and other classes and will listen for connections till they arrive. When they do, it spawns a thread 
		// which takes care of further processing. Moreover, this needs to be passed explicitly the same address that the PingPongClient
		// is using. Since this skeleton works with FactoryRemoteInterface and not RemoteInterface, it returns a reference of RemoteInterface
		// as declared in the interface FactoryRemoteInterface and taken care of in this class(PingPongServerFactory) by overriding 
		// makePingServer.
		
		Skeleton<FactoryRemoteInterface> factorySkeleton = new Skeleton<FactoryRemoteInterface>(FactoryRemoteInterface.class, pingPongServerFactory, new InetSocketAddress(factoryPort));//new InetSocketAddress(factoryPort));
		
		factorySkeleton.start();
		
	}
	// This method is called by the PingPongClient after it receives a reference to FactoryRemoteInterface 
	// (as the listener above connected this to pingPongServerFactory(instance of PingPongServerFactory created in the main method above)).
	// After that, it does a similar thing as above to get a reference of PingPongServer in the form of RemoteInterface. Then an instance of
	// stub reference is returned for the initial call since otherwise there would be no way for the stub(on the PingPongClient side)
	// to connect to the PingPongServer. After returning the stub, we can actually connect to the PingPongServer through the instance of the
	// stub returned as it is connected to the PingPongServer via the skeleton passed in this line(RemoteInterface stub = Stub.create(RemoteInterface.class, skeleton);)
	// The PingPongClient then finally calls the ping method on the PingPongServer using the stub to actually get the output "Pong IdNumber"
	// which will be handled by our MyInvocationHandler
	public RemoteInterface makePingServer() throws RMIException
	{
		try
		{
			PingPongServer pingPongServer = new PingPongServer();
			
			Skeleton<RemoteInterface> skeleton = new Skeleton<RemoteInterface>(RemoteInterface.class, pingPongServer, new InetSocketAddress(serverPort));
			
			skeleton.start();
			
			RemoteInterface stub = Stub.create(RemoteInterface.class, skeleton);
			
			return stub;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}