#!/bin/bash

#stopping previous server and client containers
docker stop server1
docker stop client1

#removing previous server and client containers
docker rm server1
docker rm client1

#building server and client containers via dockerfiles
docker build -t client:2.0 ./Client_Side
docker build -t server:2.0 ./Server_Side

#creating daemonized containers on the localhost and running necessary commands for the output
docker run -itd --net=host --name server1 server:2.0 sh -c "cd /code;make clean;make all-classes; javac *.java; java PingPongServerFactory"
docker run -itd --net=host --name client1 client:2.0 sh -c "cd /code;make clean;make all-classes; javac *.java; java PingPongClient"

#waiting for program execution
#echo "Waiting for output to be displayed for 20 seconds"
#sleep 20

#Printing the output
echo "OUTPUT:"
docker logs -f client1