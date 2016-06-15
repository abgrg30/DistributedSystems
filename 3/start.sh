#!/bin/bash

echo "Starting project"
cd code
source resize-cluster.sh 5
source start-container.sh 5

echo "project ended"
