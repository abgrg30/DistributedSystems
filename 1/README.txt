Lab 1: RMI Library for Java

Team Members:
Abhinav Garg - abgarg@eng.ucsd.edu - A53095668
Rishabh Singh Verma - rsverma@eng.ucsd.edu - A53103069

Contents of zip file:

1. Client Side and Server side files: Both share the common RMI Library in the similar structure as given for the assignment along with the tests and make files.
2. Different Files:
	1. Client_Side: PingPongClient.java , (Used interfaces: FactoryRemoteInterface and RemoteInterface)
	2. Server Side: PingPongServerFactory.java , PingPongServer.java (Used interfaces: FactoryRemoteInterface and RemoteInterface)

Required:
1. Make installation
2. Java 7
3. Ubuntu, Windows 7
4. Docker setup for runnning ping tests
	
Testing:
1. RMI: Open the directory Client_Side or Server_Side in terminal, and run command "make test" to test the RMI.
2. Ping tests: Run the shell file "start.sh" in the docker terminal

The RMI library contains two additional files: FactoryRemoteInterface and RemoteInterface, for use in ping tests(which are not actually part of the RMI Library)