#!/bin/bash


set -e # Questo serve per terminare lo script in caso di fallimento di uno qualsiasi degli step
SCRIPTS_DIR="scripts"
BUILD_UBER_JAR_COMMAND="buildUberJar.sh"
EXTRACT_PLAYER_CLASSES_COMMAND="extractPlayerClasses.sh"
#Primo step, build del uber jar
echo "Building Uber Jar"
./$SCRIPTS_DIR/$BUILD_UBER_JAR_COMMAND
#Secondo step, estrazione delle classi dei player
echo "Extracting Player Classes"
./$SCRIPTS_DIR/$EXTRACT_PLAYER_CLASSES_COMMAND
#Terzo step, creazione dei superplayer a partire dai player. Questo step per il momento è mockato
echo "[MOCK] Superplayer creation"
echo "[MOCK] Superplayer created"
#Quarto step, torneo dei superplayer