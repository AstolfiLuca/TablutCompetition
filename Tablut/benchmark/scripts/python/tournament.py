import os
import csv
import time
import glob
import json
import random
import itertools
import subprocess
from dataclasses import dataclass
from datetime import datetime
from typing import Dict, Any

from config.config_reader import CONFIG
from config.logger import setup_logger, vmessage, verbose

log = setup_logger(__name__)
@dataclass
class Player:
    name: str
    clientName: str
    role: str
    heuristics: Dict[str, float]

    @staticmethod
    def from_dict(data: Dict[str, Any]) -> 'Player':
        return Player(
            name=data['name'],
            clientName=data['clientName'],
            role=data['role'],
            heuristics=data['heuristics']
        )

@dataclass
class Superplayer:
    superPlayerName: str
    playerW: Player
    playerB: Player

    @staticmethod
    def from_dict(data: Dict[str, Any]) -> 'Superplayer':

        return Superplayer(
            superPlayerName=data['superPlayerName'],
            playerW=Player.from_dict(data['playerW']),
            playerB=Player.from_dict(data['playerB'])
        )

def clear_old_results_csv(file_path):
    with open(file_path, 'r') as f:
        header = f.readline()

    with open(file_path, 'w') as f:
        f.write(header)

    vmessage(f"File '{file_path}' svuotato con successo (header mantenuto).", debug=True)

def clear_old_logs(folder):
    for nome_elemento in os.listdir(folder):
        percorso_completo = os.path.join(folder, nome_elemento)

        if os.path.isfile(percorso_completo):
            os.remove(percorso_completo)

    vmessage(f"Cartella {folder} svuotata con successo.", debug=True)


def load_superplayers_from_file(filename):

    log.debug(f"Tentativo di lettura dal file: {filename}")
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            data_list = json.load(f)
        return [Superplayer.from_dict(item) for item in data_list]
    except Exception as e:
        log.error(f"ERRORE sconosciuto durante la lettura: {e}")
        return []


def run_server():
    os.makedirs(CONFIG["process_log_folder"], exist_ok=True)

    log_file_path = os.path.join(CONFIG["process_log_folder"], CONFIG["server"]["log_file"])

    cmd = [
        "java",
        "-cp",
        CONFIG["server"]["jar"],
        CONFIG["server"]["main_class"]
    ] + CONFIG["server"]["parameters"]

    vmessage(f"Avvio del server... Log su: {log_file_path}", debug=True)

    with open(log_file_path, "a") as log_file:
        process = subprocess.Popen(
            cmd,
            stdout=log_file,
            stderr=log_file
        )

    vmessage(f"Processo server avviato in background con PID: {process.pid}", debug=True)

    return process

def run_client(player):
    os.makedirs(CONFIG["process_log_folder"], exist_ok=True)

    log_file_path = os.path.join(CONFIG["process_log_folder"], player.name + ".logs")

    cmd = [
        "java",
        "-cp",
        CONFIG["client"]["jar"],
        player.clientName,
        player.role,
        str(CONFIG["client"]["timeout"]),
        CONFIG["client"]["server_ip"],
        player.name,
        json.dumps(player.heuristics)
    ]

    vmessage(f"Avvio del client {player.name} con timeout {CONFIG['client']['timeout']} secondi... Log su: {log_file_path}", debug=True)

    with open(log_file_path, "a") as log_file:
        process = subprocess.Popen(
            cmd,
            stdout=log_file,
            stderr=log_file
        )

    vmessage(f"Processo Client avviato in background con PID: {process.pid}", debug=True)

    return process


def match_bw_players(p1, p2):
    vmessage("Avvio del server...", debug=True)
    server_process = run_server()
    time.sleep(1)

    vmessage(f"Avvio del client {p1.role} con nome {p1.name}...", debug=True)
    client1_process = run_client(p1)
    time.sleep(1)

    vmessage(f"Avvio del client {p2.role} con nome {p2.name}...", debug=True)
    client2_process = run_client(p2)
    time.sleep(1)

    for process in [server_process, client1_process, client2_process]:
        process.wait()
        vmessage(f"Processo (PID: {process.pid}) ha terminato.", debug=True)

    vmessage("Tutti i processi del game hanno terminato", debug=True)

def match_bw_superplayers(sp1, sp2):
    vmessage(f"Game_1: WHITE: {sp1.playerW.name} vs BLACK: {sp2.playerB.name}")
    match_bw_players(sp1.playerW, sp2.playerB)

    vmessage(f"Game_2: WHITE: {sp2.playerW.name} vs BLACK: {sp1.playerB.name}")
    match_bw_players(sp1.playerB, sp2.playerW)


def write_on_csv(filename, headers, row):
    vmessage(f"Scrivendo i risultati su {filename}", debug=True)

    with open(filename, mode='a', newline='') as file_csv:
        writer = csv.writer(file_csv, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

        if not os.path.exists(filename):
            writer.writerow(headers)

        writer.writerow(row)

def lookup_match_results(playerW, playerB):
    pattern = os.path.join(
        CONFIG["process_log_folder"],
        f"_{playerW.name}_vs_{playerB.name}_*"
    )

    files_found = glob.glob(pattern)

    filename = files_found[0]

    vmessage(f"Log del game salvato in {filename}", debug=True)

    with open(filename, 'r') as f:
        rows = f.readlines()

        for row in reversed(rows):
            if row:
                return row.strip()

def store_match_results(sp1, sp2, mock=False):
    headers = ["Timestamp", "SuperPlayer_1", "SuperPlayer_2", "Punteggio_SP1", "Punteggio_SP2"]
    format = "%d-%m-%Y %H:%M"
    timestamp = datetime.now().strftime(format)
    if not mock:
        res1 = lookup_match_results(sp1.playerW, sp2.playerB)
        res2 = lookup_match_results(sp2.playerW, sp1.playerB)

        sp1_points = 0

        match res1:
            case "WW":
                sp1_points += 1
            case "D":
                sp1_points += 0.5
            case "BW":
                sp1_points += 0
            case _:
                log.error(f"Risultato non valido: {res1}")
                raise ValueError("Risultato non valido")

        match res2:
            case "WW":
                sp1_points += 0
            case "D":
                sp1_points += 0.5
            case "BW":
                sp1_points += 1
            case _:
                log.error(f"Risultato non valido: {res2}")
                raise ValueError("Risultato non valido")
        sp2_points = 2-sp1_points
        match_result_row = (timestamp, sp1.superPlayerName, sp2.superPlayerName, sp1_points,sp2_points)

    else:
        rand_points = random.choice([0, 0.5, 1, 1.5, 2])
        sp1_points = rand_points
        sp2_points = 2-rand_points
        match_result_row = (timestamp, sp1.superPlayerName, sp2.superPlayerName, sp1_points,sp2_points)

    if "single_match" in CONFIG and CONFIG["single_match"]:
        write_on_csv(CONFIG["single_match_result_file"], headers, match_result_row)
        log.info(f"{match_result_row[1]} : {match_result_row[3]}, {match_result_row[2]} : {match_result_row[4]}")
    else:
        write_on_csv(CONFIG["tournament_result_by_generation_file"], headers, match_result_row)
        write_on_csv(CONFIG["tournament_result_history_file"], headers, match_result_row)


def run_tournament(superplayers_file, mock=False):
    # Pulisco i risultati del precedente torneo
    clear_old_logs(CONFIG["process_log_folder"])
    clear_old_results_csv(CONFIG["tournament_result_by_generation_file"])

    # Ottengo i superplayers attuali
    superplayers = load_superplayers_from_file(superplayers_file)

    # Creo le coppie ed inizio il torneo
    for sp1, sp2 in itertools.combinations(superplayers, 2):
        log.info(f"Match: {sp1.superPlayerName} vs {sp2.superPlayerName}")

        if not mock:
            match_bw_superplayers(sp1, sp2)

        store_match_results(sp1, sp2, mock)

    log.info("Torneo terminato")



if __name__ == "__main__":
    #single match
    list_superplayers = load_superplayers_from_file(CONFIG["single_match_superplayers"])
    if len(list_superplayers) != 2:
        log.error("Puoi fare il match singolo solo fra due superplayer")
        exit(1)
    else:
        CONFIG["server"]["parameters"] = ["-g","-t", "2000"]
        CONFIG["server"]["log_file"] = 'server_single_match.logs'
        CONFIG["client"]["timeout"] = 1
        CONFIG["single_match"] = True
        clear_old_results_csv(CONFIG["single_match_result_file"])
        sp1 = list_superplayers[0]
        sp2 = list_superplayers[1]
        sp1.superPlayerName += '_sm'
        sp2.superPlayerName += '_sm'
        log.info(f"Match tra {sp1.superPlayerName} e {sp2.superPlayerName}")
        match_bw_superplayers(sp1,sp2)
        store_match_results(sp1,sp2)













