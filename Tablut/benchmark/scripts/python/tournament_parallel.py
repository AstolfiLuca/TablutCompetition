import os
import csv
import time
import glob
import json
import random
import socket
import itertools
import subprocess
from datetime import datetime
from multiprocessing import Lock, Manager, Pool, Value
from concurrent.futures import ThreadPoolExecutor, as_completed

import psutil

from config.config_reader import CONFIG
from config.logger import setup_logger, vmessage

log = setup_logger(__name__)

n_combination_lock = Lock()
n_combination = Value('i', 1)

# Global manager for shared locks (initialized in run_tournament)
csv_lock = None

def get_free_ports(count=1):

    ports = []
    sockets = []
    try:
        for _ in range(count):
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.bind(('', 0))
            ports.append(s.getsockname()[1])
            sockets.append(s)
    finally:
        for s in sockets:
            s.close()
    return ports


def init_worker(lock):
    global csv_lock

    csv_lock = lock

def clear_old_results_csv(file_path):
    # Crea il file se non esiste o legge l'header se esiste
    if not os.path.exists(file_path):
        with open(file_path, 'w') as f:
            pass # Crea file vuoto
        return

    with open(file_path, 'r') as f:
        header = f.readline()

    with open(file_path, 'w') as f:
        f.write(header)

    vmessage(f"File '{file_path}' svuotato con successo (header mantenuto).", debug=True)


def clear_old_logs(folder):
    if not os.path.exists(folder):
        os.makedirs(folder)
        return

    for nome_elemento in os.listdir(folder):
        percorso_completo = os.path.join(folder, nome_elemento)

        if os.path.isfile(percorso_completo):
            try:
                os.remove(percorso_completo)
            except Exception as e:
                vmessage(f"Impossibile rimuovere {percorso_completo}: {e}", error=True)

    vmessage(f"Cartella {folder} svuotata con successo.", debug=True)


def load_superplayers_from_file(filename):
    vmessage(f"Tentativo di lettura dal file: {filename}", debug=True)

    with open(filename, 'r') as f:
        superplayers = json.load(f)
    
    return superplayers


def run_server(white_port, black_port):
    os.makedirs(CONFIG["process_log_folder"], exist_ok=True)

    cmd = [
        "java",
        "-cp",
        CONFIG["server"]["jar"],
        CONFIG["server"]["main_class"]
    ] + CONFIG["server"]["parameters"] + ["-wp", str(white_port), "-bp", str(black_port)]


    if CONFIG["gen_alg_systemlog_server"]:
        # Nome log univoco basato sulle porte
        log_file_path = os.path.join(CONFIG["process_log_folder"], f"Srv_{white_port}_{black_port}" + CONFIG["server"]["log_file"])
        vmessage(f"Avvio del server... il file di log è: {log_file_path}", debug=True)
        with open(log_file_path, "a") as log_file:
            process = subprocess.Popen(
                cmd,
                stdout=log_file,
                stderr=log_file
            )
    else:
        process = subprocess.Popen(
            cmd,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )


    vmessage(f"Processo server avviato in background con PID: {process.pid}", debug=True)

    return process


def run_client(player, port):
    os.makedirs(CONFIG["process_log_folder"], exist_ok=True)

    # Aggiungiamo la porta al nome del log per evitare conflitti se lo stesso player gioca 2 game contemporanei
    log_file_path = os.path.join(CONFIG["process_log_folder"], f"{player['name']}_{port}.logs")

    cmd = [
        "java",
        "-cp",
        CONFIG["client"]["jar"],
        player["clientName"],
        player["role"], 
        str(CONFIG["client"]["timeout"]), 
        CONFIG["client"]["server_ip"], 
        player["name"], 
        json.dumps(player["heuristics"]),
        str(port),
        str(CONFIG["client"]["activate_verbose_heuristics_logs"])
    ]

    vmessage(f"Avvio del client {player['name']} con timeout {CONFIG['client']['timeout']} secondi... Log su: {log_file_path}", debug=True)

    if CONFIG["gen_alg_systemlog_client"]:
        vmessage(f"Avvio del client {player['name']} con timeout {CONFIG['client']['timeout']} secondi... Log su: {log_file_path}", debug=True)
        with open(log_file_path, "a") as log_file:
            process = subprocess.Popen(
                cmd,
                stdout=log_file,
                stderr=log_file
            )
    else:
        process = subprocess.Popen(
            cmd,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )

    vmessage(f"Processo Client avviato in background con PID: {process.pid}", debug=True)

    return process


def match_bw_players(p1, p2):
    try:
        ports = get_free_ports(2)
        white_port = ports[0]
        black_port = ports[1]
    except Exception as e:
        log.error(f"Errore nel reperire porte libere: {e}")
        return

    log.info(f"{p1['name']}_vs_{p2['name']}_ - ports w={white_port} b={black_port}")
    vmessage(f"Avvio match su porte dinamiche W:{white_port} B:{black_port}", debug=True)
    server_process = run_server(white_port=white_port, black_port=black_port)
    time.sleep(2)

    vmessage(f"Avvio del client {p1['role']} con nome {p1['name']}...", debug=True)
    client1_process = run_client(p1, port=white_port)
    time.sleep(1)
    vmessage(f"Avvio del client {p2['role']} con nome {p2['name']}...", debug=True)
    client2_process = run_client(p2, port=black_port)
    time.sleep(1)
    # Timeout di sicurezza per evitare processi appesi per sempre
    TIMEOUT_GLOBAL = 1800 # 30 minuti max per partita

    processes = [server_process, client1_process, client2_process]

    for p in processes:
        try:
            p.wait(timeout=TIMEOUT_GLOBAL)
        except subprocess.TimeoutExpired:
            vmessage(f"Processo {p.pid} in timeout. Kill forzato.", error=True)
            p.kill()

    vmessage("Tutti i processi del game hanno terminato", debug=True)


def run_single_game(sp_white, sp_black, game_num):
    global n_combination, n_combination_lock

    with n_combination_lock:
        vmessage(f"{str(n_combination.value).rjust(3)} - Game_{game_num}: {sp_white['playerW']['name']}_vs_{sp_black['playerB']['name']}_", debug=True)
        n_combination.value += 1

    match_bw_players(sp_white["playerW"], sp_black["playerB"])


def match_bw_superplayers(sp1, sp2):
    with ThreadPoolExecutor(max_workers=2) as executor:
        game1 = executor.submit(run_single_game, sp1, sp2, 1)
        game2 = executor.submit(run_single_game, sp2, sp1, 2)

        for future in as_completed([game1, game2]):
            try:
                future.result()
            except Exception as e:
                vmessage(f"Errore durante l'esecuzione del game: {e}", error=True)


def write_on_csv(filename, headers, row):
    vmessage(f"Scrivendo i risultati su {filename}", debug=True)

    with csv_lock:
        with open(filename, mode='a', newline='') as file_csv:
            writer = csv.writer(file_csv, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

            if not os.path.exists(filename):
                writer.writerow(headers)

            writer.writerow(row)


def lookup_match_results(playerW, playerB):
    pattern = os.path.join(
        CONFIG["process_log_folder"], 
        f"_{playerW['name']}_vs_{playerB['name']}_*"
    )

    max_retries = 3
    retry_delay = 1.0
    files_found = []
    for attempt in range(max_retries):
        files_found = glob.glob(pattern)
        if files_found:
            break

        if attempt < max_retries - 1:

            time.sleep(retry_delay)

    if not files_found:
        vmessage(f"Nessun log trovato per pattern {pattern} dopo {max_retries} tentativi. Ritorno D (DUMMY)", error=True)
        return "D"

    # Prendi il file più recente se ce ne sono più di uno
    files_found.sort(key=os.path.getmtime, reverse=True)
    filename = files_found[0]

    vmessage(f"Log del game trovato con nome {filename}", debug=True)

    with open(filename, 'r') as f:
        rows = f.readlines()

        for row in reversed(rows):
            if row.strip():
                return row.strip()
    vmessage("Ritorno D come risultato del game", error=True)
    return "D" # Fallback


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

def run_single_match(args):
    sp1, sp2, mock = args

    if not (sp1['superPlayerName'].endswith('ghost') and sp2['superPlayerName'].endswith('ghost')):
        uniqueId = f"{sp1['superPlayerName']}_vs_{sp2['superPlayerName']}"
        vmessage(f"{uniqueId} - AVVIATO")

        if not mock:
            match_bw_superplayers(sp1, sp2)

        vmessage(f"{uniqueId} - TERMINATO")
        store_match_results(sp1, sp2, mock)
        vmessage(f"{uniqueId} - SALVATO")


def run_tournament(superplayers_file, mock=False):
    global csv_lock

    # Pulisco risultati precedenti
    clear_old_logs(CONFIG["process_log_folder"])
    clear_old_results_csv(CONFIG["tournament_result_by_generation_file"])

    # Ottengo superplayers
    superplayers = load_superplayers_from_file(superplayers_file)

    # Creo le coppie che si sfideranno -
    combinations = [(sp1, sp2, mock) for (sp1, sp2) in itertools.combinations(superplayers, 2)]

    log.info(f"Totale match da eseguire: {len(combinations)} ({len(combinations) * 2} Games)")

    num_core = psutil.cpu_count(logical=False) - 1
    log.info(f"Sto usando {num_core} core al 100%. In questo modo la ricerca è efficiente")
    num_processes = max(1, num_core)

    # Parallelizzazione semplice
    with Manager() as manager:
        csv_lock = manager.Lock()

        with Pool(processes=num_processes, initializer=init_worker, initargs=(csv_lock,)) as p:
            p.map(run_single_match, combinations)

        csv_lock = None

    with n_combination_lock:
        n_combination.value = 1

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
        CONFIG["client"]["timeout"] = 10
        CONFIG["single_match"] = True
        clear_old_results_csv(CONFIG["single_match_result_file"])
        sp1 = list_superplayers[0]
        sp2 = list_superplayers[1]
        sp1['playerW']['name'] += '_sm'
        sp1['playerB']['name'] += '_sm'
        sp2['playerW']['name'] += '_sm'
        sp2['playerB']['name'] += '_sm'
        log.info(f"Match tra {sp1['superPlayerName']} e {sp2['superPlayerName']}")
        match_bw_superplayers(sp1,sp2)
        store_match_results(sp1,sp2)