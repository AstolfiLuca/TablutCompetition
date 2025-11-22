#!/bin/bash


set -e # Questo serve per terminare lo script in caso di fallimento di uno qualsiasi degli step
SCRIPTS_DIR="scripts/bash"
BUILD_UBER_JAR_COMMAND="buildUberJar.sh"

#Primo step, build del uber jar
echo "Building Uber Jar"
./$SCRIPTS_DIR/$BUILD_UBER_JAR_COMMAND
