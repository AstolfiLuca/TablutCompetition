#!/bin/bash

ROLE=$1
TIMEOUT=$2
SERVER_IP=$3
NAME="CrewPlayer"
UBER_JAR="tablut_uber_jar.jar"
HEURISTICS=""

echo "Running player with role $ROLE, timeout $TIMEOUT and server ip $SERVER_IP"

java -cp $UBER_JAR "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline.BaselinePlayer" $ROLE $TIMEOUT $SERVER_IP $NAME $HEURISTICS

