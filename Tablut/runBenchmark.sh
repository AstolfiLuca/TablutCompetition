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

#MAIN_JAR="./target/tablut_benchmark_jar.jar"
#SERVER_CLASS="it.unibo.ai.didattica.competition.tablut.server.Server"
#SERVER_PARAMETERS="-g -t 2000"
#LOGS_FOLDER="${TARGET_DIR}/logs"
#
#mkdir "$LOGS_FOLDER"
#
#touch "${LOGS_FOLDER}/server.logs"
#echo "Starting server"
#java -cp $MAIN_JAR $SERVER_CLASS $SERVER_PARAMETERS >> "${LOGS_FOLDER}/server.logs" 2>&1 &
## Cattura i PID visibili da git bash (importante per controlli/kill)
#SERVER_PID=$!
#echo "Server launched with parameters $SERVER_PARAMETERS and PID: $SERVER_PID."
#sleep 1
#
#java -cp $MAIN_JAR "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.random.TablutRandomWhiteClient"  &
#WHITE_PID=$!
#echo "White launched with PID: $WHITE_PID."
#sleep 1
#
#java -cp "$MAIN_JAR" "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.random.TablutRandomBlackClient" &
#BLACK_PID=$!
#echo "Black launched with PID: $BLACK_PID."
#sleep 1



INPUT_FILE=$OUTPUT_CLASS_LIST

# Array "principale" (la lista degli ID) conetenente i nomi semplici, es: "BaselineBlackPlayer"
declare -a player_list

# Array associativi che usano il nome semplice come chiave (es. BaselineBlackPlayer)
declare -A player_name
declare -A player_color
declare -A player_class

#Lettura del file e popolamento lista player
echo "Reading $INPUT_FILE to extract players..."

if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: File $INPUT_FILE not found."
    exit 1
fi

while IFS= read -r row; do
    [ -z "$row" ] && continue # Ignora righe vuote

    # La riga intera è il nome della classe
     class_name="$row"

    #Estrae l'ID (il nome semplice del player)
    #"it.unibo...BaselineBlackPlayer" -> "BaselineBlackPlayer"
     id_player="${class_name##*.}"

    #Estrae il colore
     color=""
    if [[ "$id_player" == *BlackPlayer ]]; then
        color="Black"
    elif [[ "$id_player" == *WhitePlayer ]]; then
        color="White"
    else
        echo "Warnind: colors must be black or white: '$class_name'. Ignored."
        continue
    fi
    # --- "Costruzione dell'oggetto tramite array associativi" ---

    # Aggiunge l'ID alla lista principale
    player_list+=("$id_player")

    # Popola i "campi" nelle mappe, usando l'ID come chiave
    player_name["$id_player"]="$id_player"
    player_color["$id_player"]="$color"
    player_class["$id_player"]="$class_name"

done < "$INPUT_FILE"

echo "Extraction completed."
echo "Found: ${#player_list[@]} players"


# Creazione "Oggetti" SuperPlayer
echo "Creation SuperPlayer..."

#Strutture Dati SuperPlayer
declare -a superplayer_list # La lista principale di ID (es: "BaselineBlack_MyWhite_SP")
declare -A superplayer_black_component # Mappa: [SP_ID] -> ID_Player_Nero
declare -A superplayer_white_component # Mappa: [SP_ID] -> ID_Player_Bianco

# Smistamento Player Bianchi e Neri
declare -a black_player_ids
declare -a white_player_ids

for id in "${player_list[@]}"; do
    colore="${player_color[$id]}"
    if [ "$colore" == "Black" ]; then
        black_player_ids+=("$id")
    elif [ "$colore" == "White" ]; then
        white_player_ids+=("$id")
    fi
done

echo "Black Player Found: ${#black_player_ids[@]}"
echo "White Player Found: ${#white_player_ids[@]}"


echo "Generating SuperPlayer..."

# Controlla se abbiamo entrambi i tipi di player
if [ ${#black_player_ids[@]} -eq 0 ] || [ ${#white_player_ids[@]} -eq 0 ]; then
    echo "Error: some players are missing."
    exit 1
fi

for black_id in "${black_player_ids[@]}"; do
    for white_id in "${white_player_ids[@]}"; do

        sp_id="${black_id}_${white_id}_SP"


        superplayer_list+=("$sp_id")

        #Popola i "campi" del SuperPlayer (i suoi componenti)
        superplayer_black_component["$sp_id"]="$black_id"
        superplayer_white_component["$sp_id"]="$white_id"

    done
done

echo "SuperPlayer creation completed"
for sp_id in "${superplayer_list[@]}"; do
  echo $sp_id
done











