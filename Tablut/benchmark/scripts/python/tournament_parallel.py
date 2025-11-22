import os
import csv
import time
import glob
import json
import random
import itertools
import subprocess
from datetime import datetime
from multiprocessing import Lock, Manager, Pool, Value

from config.config_reader import CONFIG
from config.logger import setup_logger, vmessage, verbose

log = setup_logger(__name__)

n_combination_lock = Lock()
n_combination = Value('i', 1)

# Global manager for shared locks (initialized in run_tournament)
_manager = None
_csv_lock = None

# --------------------------
#   PARALLEL WORKER
# --------------------------

def _init_worker(lock):
    """Initialize worker process with shared lock"""
    global _csv_lock
    _csv_lock = lock

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
    vmessage(f"Tentativo di lettura dal file: {filename}", debug=True)

    with open(filename, 'r') as f:
        superplayers = json.load(f)
    
    return superplayers


def run_server(white_port, black_port):
    os.makedirs(CONFIG["process_log_folder"], exist_ok=True)

    log_file_path = os.path.join(CONFIG["process_log_folder"], str(white_port) + str(black_port) + CONFIG["server"]["log_file"])

    cmd = [
        "java",
        "-cp",
        CONFIG["server"]["jar"],
        CONFIG["server"]["main_class"]
    ] + CONFIG["server"]["parameters"] + ["-wp", str(white_port), "-bp", str(black_port)]

    vmessage(f"Avvio del server... Log su: {log_file_path}", debug=True)

    process = subprocess.Popen(
        cmd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL
    )

    vmessage(f"Processo server avviato in background con PID: {process.pid}", debug=True)

    return process


def run_client(player, port):
    os.makedirs(CONFIG["process_log_folder"], exist_ok=True)

    log_file_path = os.path.join(CONFIG["process_log_folder"], player["name"] + ".logs")

    cmd = [
        "java",
        "-cp",
        CONFIG["client"]["jar"],
        player["clientName"],
        player["role"], 
        str(CONFIG["client"]["timeout"]), 
        CONFIG["client"]["server_ip"], 
        player["name"], 
        json.dumps(player["heuristics"])
    ] + [str(port)]

    vmessage(
        f"Avvio del client {player['name']} con timeout {CONFIG['client']['timeout']} secondi... Log su: {log_file_path}",
        debug=True
    )

    process = subprocess.Popen(
        cmd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL
    )

    vmessage(f"Processo Client avviato in background con PID: {process.pid}", debug=True)

    return process


def match_bw_players(p1, p2, port=None):
    white_port = port
    black_port = white_port + 1

    vmessage("Avvio del server...", debug=True)
    server_process = run_server(white_port=white_port, black_port=black_port)
    time.sleep(1)

    client1_port =  white_port if str(p1['role']).lower() == 'white' else black_port
    vmessage(f"Avvio del client {p1['role']} con nome {p1['name']}...", debug=True)
    client1_process = run_client(p1, port=client1_port)
    time.sleep(1)

    client2_port =  white_port if str(p2['role']).lower() == 'white' else black_port
    vmessage(f"Avvio del client {p2['role']} con nome {p2['name']}...", debug=True)
    client2_process = run_client(p2, port=client2_port)
    time.sleep(1)

    for process in [server_process, client1_process, client2_process]:
        process.wait()
        vmessage(f"Processo (PID: {process.pid}) ha terminato.", debug=True)

    vmessage("Tutti i processi del game hanno terminato", debug=True)


def match_bw_superplayers(sp1, sp2, port):
    global n_combination, n_combination_lock

    with n_combination_lock:
        log.info(f"{str(n_combination.value).rjust(3)} - Game_1: WHITE: {sp1['playerW']['name']} vs BLACK: {sp2['playerB']['name']}")
        n_combination.value += 1

    match_bw_players(sp1["playerW"], sp2["playerB"], port)

    with n_combination_lock:
        log.info(f"{str(n_combination.value).rjust(3)} - Game_2: WHITE: {sp2['playerW']['name']} vs BLACK: {sp1['playerB']['name']}")
        n_combination.value += 1

    match_bw_players(sp2["playerW"], sp1["playerB"], port)



def write_on_csv(filename, headers, row):
    vmessage(f"Scrivendo i risultati su {filename}", debug=True)

    file_exists = os.path.exists(filename)

    with _csv_lock:
        with open(filename, mode='a', newline='') as file_csv:
            writer = csv.writer(file_csv, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

            if not file_exists:
                writer.writerow(headers)

            writer.writerow(row)


def lookup_match_results(playerW, playerB):
    pattern = os.path.join(
        CONFIG["process_log_folder"], 
        f"_{playerW['name']}_vs_{playerB['name']}_*"
    )

    files_found = glob.glob(pattern)

    if not files_found:
        raise FileNotFoundError(f"Nessun log trovato per pattern {pattern}")

    filename = files_found[0]

    vmessage(f"Log del game salvato in {filename}", debug=True)

    with open(filename, 'r') as f:
        rows = f.readlines()
        
        for row in reversed(rows):
            if row:
                return row.strip()


def store_match_results(sp1, sp2, mock=False):
    if not mock:
        res1 = lookup_match_results(sp1["playerW"], sp2["playerB"])
        res2 = lookup_match_results(sp2["playerW"], sp1["playerB"])
        
        sp1_points = 0

        match res1:
            case "WW":
                sp1_points += 1
            case "D":
                sp1_points += 0.5
            case "BW":
                sp1_points += 0
            case _:
                vmessage(f"Risultato non valido: {res1}", error=True)
                raise ValueError("Risultato non valido")

        match res2:
            case "WW":
                sp1_points += 0
            case "D":
                sp1_points += 0.5
            case "BW":
                sp1_points += 1
            case _:
                vmessage(f"Risultato non valido: {res2}", error=True)
                raise ValueError("Risultato non valido")

    else:
        sp1_points = random.choice([0, 0.5, 1, 1.5, 2])
    
    sp2_points = 2 - sp1_points

    headers = ["Timestamp", "SuperPlayer_1", "SuperPlayer_2", "Punteggio_SP1", "Punteggio_SP2"]

    row = (
        datetime.now().strftime("%d-%m-%Y %H:%M"),
        sp1["superPlayerName"],
        sp2["superPlayerName"],
        sp1_points,
        sp2_points
    )

    write_on_csv(CONFIG["tournament_result_by_generation_file"], headers, row)
    write_on_csv(CONFIG["tournament_result_history_file"], headers, row)


# --------------------------
#   PARALLEL WORKER
# --------------------------

def _run_single_match(args):
    sp1, sp2, mock, idx = args
    # Offset to reintantiate next white/black ports
    offset = 3

    port = CONFIG["port"] + (idx * 2) + offset

    if not mock:
        match_bw_superplayers(sp1, sp2, port)

    store_match_results(sp1, sp2, mock)


# --------------------------
#   TOURNAMENT MAIN
# --------------------------

def run_tournament(superplayers_file, mock=False):
    global _manager, _csv_lock

    # Pulisco risultati precedenti
    clear_old_logs(CONFIG["process_log_folder"])
    clear_old_results_csv(CONFIG["tournament_result_by_generation_file"])

    # Ottengo superplayers
    superplayers = load_superplayers_from_file(superplayers_file)

    # Creo le coppie che si sfideranno - con idx per la successione delle porte
    combinations = [(sp1, sp2, mock, idx) for idx, (sp1, sp2) in enumerate(itertools.combinations(superplayers, 2))]

    log.info(f"Totale match da eseguire: {len(combinations)} ({len(combinations) * 2} Games)")

    # Default: usa meta' delle risorse per non oversaturare
    num_processes = max(1, os.cpu_count() - 2)

    # Parallelizzazione semplice
    with Manager() as manager:
        _manager = manager


        csv_lock = manager.Lock()
        with Pool(processes=num_processes, initializer=_init_worker, initargs=(csv_lock,)) as p:
            p.map(_run_single_match, combinations)
                # Clean up globals
        
        _manager = None
        _csv_lock = None

    log.info("Torneo terminato")
