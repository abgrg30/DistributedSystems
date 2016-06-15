#!/bin/bash

/root/start-hadoop.sh
serf members
echo "master running ....."
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar

/usr/local/hadoop/bin/hadoop fs -copyFromLocal samplefile.txt /input
/usr/local/hadoop/bin/hadoop com.sun.tools.javac.Main Bigram.java
jar cf wc.jar Bigram*.class
/usr/local/hadoop/bin/hadoop jar wc.jar Bigram /input /output
