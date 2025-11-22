#!/bin/bash

PROJECT_DIR="../" # Directory root del progetto (dove si trova src e build.xml)
BENCHMARK_DIR="./benchmark/"
TARGET_DIR="${BENCHMARK_DIR}target"
ANT_BUILD="${PROJECT_DIR}build.xml"


#Parte di script che si integra con ant per comodita.
#Nel file build.xml di ant devono essere specificate le librerie usate dal progetto
JAR_COMMAND="uber-jar"
echo "Launching Ant Command $JAR_COMMAND"
ant -f $ANT_BUILD $JAR_COMMAND -Ddest.dir=$TARGET_DIR

echo "Uber jar created"
