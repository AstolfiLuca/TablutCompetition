#!/bin/bash
UBER_JAR="tablut_uber_jar.jar"
echo "Running server..."

java -cp $UBER_JAR "it.unibo.ai.didattica.competition.tablut.server.Server" "-g -t 2000"

