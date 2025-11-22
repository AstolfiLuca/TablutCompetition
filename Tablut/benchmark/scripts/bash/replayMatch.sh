#!/bin/bash

read -p "Insert log file name: " MATCH_LOGS_FILE

MAIN_JAR="./target/tablut_benchmark_jar.jar"
SERVER_CLASS="it.unibo.ai.didattica.competition.tablut.server.Server"
LOG_DIR="./logs"
SERVER_PARAMETERS="-g -R ${LOG_DIR}/${MATCH_LOGS_FILE}"


java -cp $MAIN_JAR $SERVER_CLASS $SERVER_PARAMETERS