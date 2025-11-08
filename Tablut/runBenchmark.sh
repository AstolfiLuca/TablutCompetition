#!/bin/bash

set -e # Questo serve per terminare lo script in caso di fallimento di uno qualsiasi degli step
PROJECT_DIR="./" # Directory root del progetto (dove si trova src e build.xml)
SRC_DIR="${PROJECT_DIR}src"
TARGET_DIR="${PROJECT_DIR}target"
BASE_PACKAGE_PATH="it/unibo/ai/didattica/competition/tablut" # Questo DEVE riflettere la struttura completa del package in 'src'
PLAYERS_PACKAGE="${BASE_PACKAGE_PATH}/client/tablutcrew/clients" # Il package dove si trovano le classi Player

# Costanti di ricerca
BLACK_SUFFIX="BlackPlayer"
WHITE_SUFFIX="WhitePlayer"
# Regex per trovare i file sorgente dei player
PLAYER_REGEX=".*(${BLACK_SUFFIX}|${WHITE_SUFFIX})\.java$"
OUTPUT_CLASS_LIST="${TARGET_DIR}/player_classes_list.txt"

#Parte di script che si integra con ant per comodita.
#Nel file build.xml di ant devono essere specificate le librerie usate dal progetto
JAR_COMMAND="uber-jar"
echo "Launching Ant Command $JAR_COMMAND"
ant $JAR_COMMAND #TODO tightly coupled con ant, si può separare la logica passandogli argomenti in input

echo "Uber jar created"
echo "Cleaning target files..."
# Rimuove il vecchio elenco classi
rm -f "$OUTPUT_CLASS_LIST"
# Crea il file vuoto
touch "$OUTPUT_CLASS_LIST"

# Percorso completo in cui cercare (es. "src/clients")
SEARCH_PATH="${SRC_DIR}/${PLAYERS_PACKAGE}"
echo "Searching players in: $SEARCH_PATH"

# 1. Trova (find) tutti i file (-type f) nel percorso di ricerca.
# 2. Cerca i file che terminano (-name) con "*WhitePlayer.java" O (-o) "*BlackPlayer.java".
# 3. Esegue un ciclo (while read) per ogni file trovato (FILE_PATH).
# Attenzione questo meccanismo fa uso di una subshell
find "$SEARCH_PATH" -type f -regextype posix-extended -regex "$PLAYER_REGEX" | while read FILE_PATH
do
    # Rimuove il prefisso "src/"
    CLASS_PATH=${FILE_PATH#"$SRC_DIR/"}
    # Rimuove il suffisso ".java"
    CLASS_PATH=${CLASS_PATH%".java"}
    # Sostituisce tutti i '/' con '.' per ottenere il nome completo della classe
    FULL_CLASS_NAME=${CLASS_PATH//\//.}
    # Aggiunge il nome della classe al file di output
    echo "$FULL_CLASS_NAME" >> "$OUTPUT_CLASS_LIST"
done

# Contiamo le righe del file di output alla fine
COUNT=$(wc -l < "$OUTPUT_CLASS_LIST")
echo "Search complete"
echo "Found ${COUNT} Player classes. List saved in: $OUTPUT_CLASS_LIST"


#------------------------------------------------------------------

MAIN_JAR="./target/tablut_benchmark_jar.jar"
SERVER_CLASS="it.unibo.ai.didattica.competition.tablut.server.Server"
SERVER_PARAMETERS="-g -t 2000"
LOGS_FOLDER="${TARGET_DIR}/logs"

mkdir "$LOGS_FOLDER"

touch "${LOGS_FOLDER}/server.logs"
echo "Starting server"
java -cp $MAIN_JAR $SERVER_CLASS $SERVER_PARAMETERS >> "${LOGS_FOLDER}/server.logs" 2>&1 &
# Cattura i PID visibili da git bash (importante per controlli/kill)
SERVER_PID=$!
echo "Server launched with parameters $SERVER_PARAMETERS and PID: $SERVER_PID."
sleep 1

java -cp $MAIN_JAR "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline.BaselineWhitePlayer"  >> "${LOGS_FOLDER}/white_stout.logs" 2>&1 &
WHITE_PID=$!
echo "White launched with PID: $WHITE_PID."
sleep 1

java -cp "$MAIN_JAR" "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.tavoletta.TavolettaBlackPlayer">> "${LOGS_FOLDER}/black_stdout.logs" 2>&1 &
BLACK_PID=$!
echo "Black launched with PID: $BLACK_PID."
sleep 1








