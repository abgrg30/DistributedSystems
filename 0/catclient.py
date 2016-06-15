#!/usr/bin/python

import socket
import sys
from time import sleep

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

if sock < 0:
        #print 'Client Socket Creation Failed!!'
	sys.exit()

#host = socket.gethostname()
host = 'catserver'

sock.connect((host,int(sys.argv[2])))

f = open(sys.argv[1], 'r')

maxi = 30
interval = 3
count = 0

while count < maxi/interval:

        sock.send('LINE\n')
        rec = sock.recv(100)

        if rec == '':
		#print >> sys.stderr,'client'
		break
	else:
                rec = rec.lower()
		f.seek(0,0)
		latest = f.readline() + '\n'
		flag = 1

		while latest != '':
			if latest == rec:
		        	print >> sys.stderr,'OK\n'
				flag = 0
				break
			else:
				latest = f.readline() + '\n'

		if flag == 1:				
			print >> sys.stderr,'MISSING\n'

		count +=1
        	sleep(interval)

sock.send('quit now!!\n')

f.close()
sock.close()

