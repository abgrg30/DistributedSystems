//PingPongClient File

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import rmi.*;

public class PingPongClient
{
	private static String url = "server1";
	private static Integer factoryPort = 6000;
	
	public static void main(String[] args) throws RMIException, FileNotFoundException
	{
		FactoryRemoteInterface factoryStub = Stub.create(FactoryRemoteInterface.class, new InetSocketAddress(factoryPort));
		
		//Sleeping for a few seconds to ensure connection 
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
		
		RemoteInterface stub =  factoryStub.makePingServer();
		
		int countCorrect = 0;
		
		System.out.println("");
		System.out.println("Send ping 200");
		System.out.println("Received : "+ stub.ping(200));
		if(stub.ping(200).equals("Pong 200"))
		{
			countCorrect += 1;
		}
		
		System.out.println("");
		System.out.println("Send ping 400");
		System.out.println("Received : "+ stub.ping(400));
		if(stub.ping(400).equals("Pong 400"))
		{
			countCorrect += 1;
		}
		
		System.out.println("");
		System.out.println("Send ping 600");
		System.out.println("Received : "+ stub.ping(600));
		if(stub.ping(600).equals("Pong 600"))
		{
			countCorrect += 1;
		}
		
		System.out.println("");
		System.out.println("Send ping 800");
		System.out.println("Received : "+ stub.ping(800));
		if(stub.ping(800).equals("Pong 800"))
		{
			countCorrect += 1;
		}
		
		System.out.println("");
		System.out.println("4 Tests Completed, "+(4-countCorrect)+" Tests Failed");
		
	}
	
	
}