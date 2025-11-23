#!/bin/bash

ROLE=$1
TIMEOUT=$2
SERVER_IP=$3
NAME="CrewPlayer"
UBER_JAR="tablut_uber_jar.jar"

ROLE_LOWER=${ROLE,,}

HEURISTICS=""

if [[ "$ROLE_LOWER" == "white" ]]; then
    HEURISTICS='{"KING_THREAT_WEIGHT": 0.0,"KING_SAFETY_WEIGHT": 3.0,"KING_INCHECK_WEIGHT": 5.0,"MANHATTAN_TOKING_WEIGHT": 0.0,"MANHATTAN_TOESCAPES_WEIGHT": 13.0,"LINE_SCORE_WEIGHT": 11.0,"PAWNS_SAFETY_WEIGHT": 4.0,"PAWNS_THREAT_SCORE_WEIGHT": 14.0,"PIECES_SCORE_WEIGHT": 89.0,"DEFENSIVE_POSITION_WEIGHT": 0.0,"RHOMBUS_SCORE_WEIGHT": 0.0}'
elif [[ "$ROLE_LOWER" == "black" ]]; then
    HEURISTICS='{"KING_THREAT_WEIGHT": 4.0,"KING_SAFETY_WEIGHT": 4.0,"KING_INCHECK_WEIGHT": 3.0,"MANHATTAN_TOKING_WEIGHT": 14.0,"MANHATTAN_TOESCAPES_WEIGHT": 0.0,"LINE_SCORE_WEIGHT": 7.0,"PAWNS_SAFETY_WEIGHT": 10.0,"PAWNS_THREAT_SCORE_WEIGHT": 7.0,"PIECES_SCORE_WEIGHT": 78.0,"DEFENSIVE_POSITION_WEIGHT": 0.0,"RHOMBUS_SCORE_WEIGHT": 14.0}'
else
    echo "Errore: Ruolo non riconosciuto ($ROLE). Usa 'White' o 'Black'."
    exit 1
fi

HEURISTICS=$(echo "$HEURISTICS" | tr -d '\n' | tr -d '\r')

echo "Running player with role $ROLE, timeout $TIMEOUT and server ip $SERVER_IP"
echo "Loaded Heuristics: $HEURISTICS"

java -cp $UBER_JAR "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline.BaselinePlayer" "$ROLE" "$TIMEOUT" "$SERVER_IP" "$NAME" "$HEURISTICS"