#!/usr/bin/python

import socket
import sys

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

if s < 0:
        #print 'Server Socket Creation Failed!!'
	sys.exit()

try:
        s.bind((socket.gethostname(), int(sys.argv[2]))) #Bind socket to local host and port
except socket.error as msg:
        #print 'Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
        sys.exit()

#print 'Socket bind complete'

f = open(sys.argv[1], 'r')

if f < 0:
        #print 'File not found'
        s.close()
        sys.exit()

flag = 0
if len(f.readlines()) == 0:
        flag = 1

s.listen(1)  #Start listening on socket
#print 'Socket now listening'

conn, addr = s.accept()  #wait to accept a connection - blocking call
#print 'Connected with ' + addr[0] + ':' + str(addr[2])

while 1:

        rec = conn.recv(100)

        if rec == '':
                #print 'Server Receive Failed!!'
                break
        else:
                #print 'Server Data Received = ' + rec

                if rec == 'LINE\n':
                        if flag == 1:
                                #print >> sys.stderr,'server'
				break
                                
			else:
                        	next = f.readline()

                        	if next == '':
                                	f.seek(0,0)
                                	next = f.readline()

                        	next = next.upper() + '\n'

                        	conn.send(next)

		if rec == 'quit now!!\n':
			break
f.close()
s.close()

