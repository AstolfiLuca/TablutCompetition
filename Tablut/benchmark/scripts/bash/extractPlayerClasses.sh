#!/bin/bash

SRC_DIR="../src"
BASE_PACKAGE_PATH="it/unibo/ai/didattica/competition/tablut" # Questo DEVE riflettere la struttura completa del package in 'src'
PLAYERS_PACKAGE="${BASE_PACKAGE_PATH}/client/tablutcrew/clients" # Il package dove si trovano le classi Player
PLAYERS_DIR="./players"
# Costanti di ricerca
BLACK_SUFFIX="BlackPlayer"
WHITE_SUFFIX="WhitePlayer"
# Regex per trovare i file sorgente dei player
PLAYER_REGEX=".*(${BLACK_SUFFIX}|${WHITE_SUFFIX})\.java$"
OUTPUT_CLASS_LIST="${PLAYERS_DIR}/player_classes_list.txt"


echo "Cleaning target files..."
rm "$OUTPUT_CLASS_LIST"
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