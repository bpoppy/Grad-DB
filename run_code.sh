#!/bin/bash

# You should be able to put your code into the src folder
# and run this script from an iLab machine without modification

JAVA_HOME=/usr/local/java-archive/jdk1.7.0_11/
PATH=$JAVA_HOME/bin:/bin:/usr/bin

echo 
echo "running ant to compile code..."
ant
if [ $? != 0 ]; then
  exit 1
fi

echo 
echo "clearing output directory ..."
rm -Rf out
mkdir out

echo 
echo "running process..."
timeout -s KILL 2m java -cp "classes:lib/h2-1.3.170.jar" \
    edu.rutgers.cs541.EntryPoint sample_input/schema1.sql \
    sample_input/query1a.sql sample_input/query1b.sql out

exitCode=$?

if [ $exitCode = 124 ]; then
  echo "...process was terminated after 2 minutes"
elif [ $exitCode = 0 ]; then
  echo "...process exited before timeout with code $exitCode"
fi
