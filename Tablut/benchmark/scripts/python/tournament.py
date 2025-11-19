import csv
import glob
import itertools
import json
import random
from dataclasses import dataclass
from datetime import datetime
from typing import Dict, Any
import subprocess
import os
import time
from config.config_reader import CONFIG
from config.logger import setup_logger

log = setup_logger(__name__)
@dataclass
class Player:
    name: str
    client_name: str
    role: str
    heuristics: Dict[str, float]

    @staticmethod
    def from_dict(data: Dict[str, Any]) -> 'Player':
        return Player(
            name=data['name'],
            client_name=data['clientName'],
            role=data['role'],
            heuristics=data['heuristics']
        )

@dataclass
class Superplayer:
    super_player_name: str
    player_w: Player
    player_b: Player

    @staticmethod
    def from_dict(data: Dict[str, Any]) -> 'Superplayer':

        return Superplayer(
            super_player_name=data['superPlayerName'],
            player_w=Player.from_dict(data['playerW']),
            player_b=Player.from_dict(data['playerB'])
        )



def run_server():

    process_log_folder = CONFIG["process_log_folder"]
    log_file_path = os.path.join(process_log_folder, CONFIG["server"]["log_file"])
    os.makedirs(process_log_folder, exist_ok=True)

    cmd = [
              "java",
              "-cp",
              CONFIG["server"]["jar"],
              CONFIG["server"]["main_class"]
          ] + CONFIG["server"]["parameters"] # Aggiunge i parametri alla fine

    log.debug(f"Avvio del server... Log su: {log_file_path}")

    try:
        #Apri il file di log in modalità 'append' ('a')
        with open(log_file_path, "a") as log_file:

            process = subprocess.Popen(
                cmd,
                stdout=log_file,  # Redireziona stdout al file (come '>>')
                stderr=log_file   # Redireziona stderr allo stesso file (come '2>&1')
            )

        log.debug(f"Processo server avviato in background con PID: {process.pid}")

    except FileNotFoundError:
        log.error("ERRORE: Comando 'java' non trovato. Assicurati che sia nel PATH di sistema.")
    except Exception as e:
        log.error(f"Si è verificato un errore imprevisto: {e}")
    return process

def run_client(player):

    process_log_folder = CONFIG["process_log_folder"]
    log_file_path = os.path.join(process_log_folder, player.name+".logs")
    os.makedirs(process_log_folder, exist_ok=True)
    timeout = CONFIG["client"]["timeout"]
    server_ip = CONFIG["client"]["server_ip"]
    heuristics_dict_string = json.dumps(player.heuristics)

    cmd = [
              "java",
              "-cp",
              CONFIG["client"]["jar"],
              player.client_name
          ] + [player.role, str(timeout), server_ip , player.name, heuristics_dict_string] # Aggiunge i parametri alla fine

    log.debug(f"Avvio del client con timeout {timeout} secondi... Log su: {log_file_path}")

    try:
        # Apri il file di log in modalità 'append' ('a')
        with open(log_file_path, "a") as log_file:

            # 5. Avvia il processo
            process = subprocess.Popen(
                cmd,
                stdout=log_file,  # Redireziona stdout al file (come '>>')
                stderr=log_file   # Redireziona stderr allo stesso file (come '2>&1')
            )

        log.debug(f"Processo Client avviato in background con PID: {process.pid}")

    except FileNotFoundError:
        log.error("ERRORE: Comando 'java' non trovato. Assicurati che sia nel PATH di sistema.")
    except Exception as e:
        log.error(f"Si è verificato un errore imprevisto: {e}")
    return process


def match_bw_players(p1,p2):
    #start server
    log.debug("Avvio del server...")
    server_process = run_server()
    time.sleep(1)
    #run client p1 with heuristics in input
    log.debug(f"Avvio del client {p1.role} con nome {p1.name}...")
    client1_process = run_client(p1)
    time.sleep(1)
    #run client p2 with heuristics in input
    log.debug(f"Avvio del client {p2.role} con nome {p2.name}...")
    client2_process = run_client(p2)
    time.sleep(1)
    for process in [server_process, client1_process, client2_process]:
        process.wait()
        log.debug(f"Processo (PID: {process.pid}) ha terminato.")
    log.debug("Tutti i processi del game hanno terminato")



def match_bw_superplayers(sp1,sp2):
    # white sp1 vs black sp2
    log.info(f"Game_1: WHITE: {sp1.player_w.name} vs BLACK: {sp2.player_b.name}")
    match_bw_players(sp1.player_w,sp2.player_b)
    # black sp1 vs white sp2
    log.info(f"Game_2: WHITE: {sp2.player_w.name} vs BLACK: {sp1.player_b.name}")
    match_bw_players(sp1.player_b,sp2.player_w)



def load_superplayers_from_file(filename):

    log.debug(f"Tentativo di lettura dal file: {filename}")
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            data_list = json.load(f)

        return [Superplayer.from_dict(item) for item in data_list]

    except FileNotFoundError:
        log.error(f"ERRORE: File non trovato a '{filename}'")
        return []
    except json.JSONDecodeError:
        log.error(f"ERRORE: Il file '{filename}' non è un JSON valido o è vuoto.")
        return []
    except KeyError as e:
        log.error(f"ERRORE: Manca una chiave nel JSON: {e}. Controlla la mappatura.")
        return []
    except Exception as e:
        log.error(f"ERRORE sconosciuto durante la lettura: {e}")
        return []



def delete_previous_logs(folder):
    if not os.path.isdir(folder):
        log.error(f"Errore: Il percorso '{folder}' non è una cartella valida.")
        return
    log.debug(f"Pulizia della cartella: {folder}...")
    for nome_elemento in os.listdir(folder):
        percorso_completo = os.path.join(folder, nome_elemento)
        try:
            if os.path.isfile(percorso_completo):
                os.remove(percorso_completo)

        except Exception as e:
            log.error(f"Errore durante la cancellazione di {nome_elemento}: {e}")

def lookup_game_result(player_w, player_b):
    player_w_name = player_w.name
    player_b_name = player_b.name
    game_log_file_name = '_'+player_w_name + '_vs_' + player_b_name + '_'
    pattern = os.path.join(CONFIG["process_log_folder"], f"{game_log_file_name}*")

    files_found = glob.glob(pattern)
    try:
        filename = files_found[0]
        log.debug(f"Log del game salvato in {filename}")
    except:
        log.error("Impossibile trovare il file di log del game")
        return
    last_row=None
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            rows = f.readlines()
            for row in reversed(rows):
                riga_stripped = row.strip()

                if riga_stripped:
                    last_row = riga_stripped
                    break
    except Exception as e:
        log.error(f"Si è verificato un errore durante la lettura: {e}")
        return None

    return last_row


def write_on_csv(filename, headers, row):
    log.debug(f"Scrivendo i risultati su {filename}")
    file_exists = os.path.exists(filename)
    with open(filename, mode='a', newline='', encoding='utf-8') as file_csv:
        writer = csv.writer(file_csv, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

        if not file_exists:
            writer.writerow(headers)

        writer.writerow(row)



def store_result_of_match(sp1,sp2,mock=False):
    headers = ["Timestamp", "SuperPlayer_1", "SuperPlayer_2", "Punteggio_SP1", "Punteggio_SP2"]
    format = "%d-%m-%Y %H:%M"
    timestamp = datetime.now().strftime(format)
    if not mock:
        sp1_white_vs_sp2_black=lookup_game_result(sp1.player_w,sp2.player_b)
        sp1_black_vs_sp2_white=lookup_game_result(sp2.player_w,sp1.player_b)
        sp1_points = 0
        if sp1_white_vs_sp2_black == 'WW':
            sp1_points += 1
        elif sp1_white_vs_sp2_black == 'D':
            sp1_points += 0.5
        elif sp1_white_vs_sp2_black == 'BW':
            sp1_points += 0
        else:
            log.error("Impossibile leggere il risultato")
            raise Exception("Impossibile leggere il risultato")
        if sp1_black_vs_sp2_white == 'WW':
            sp1_points += 0
        elif sp1_black_vs_sp2_white == 'D':
            sp1_points += 0.5
        elif sp1_black_vs_sp2_white == 'BW':
            sp1_points += 1
        else:
            log.error("Impossibile leggere il risultato")
            raise Exception("Impossibile leggere il risultato")
        sp2_points = 2-sp1_points

        match_result_row = (timestamp, sp1.super_player_name, sp2.super_player_name, sp1_points,sp2_points)
        write_results(headers, match_result_row)
    else:
        rand_points = random.choice([0, 0.5, 1, 1.5, 2])
        sp1_points = rand_points
        sp2_points = 2-rand_points
        match_result_row = (timestamp, sp1.super_player_name, sp2.super_player_name, sp1_points,sp2_points)
        write_results(headers, match_result_row)

def write_results(headers, match_result_row):
    write_on_csv(CONFIG["tournament_result_by_generation_file"], headers, match_result_row)
    write_on_csv(CONFIG["tournament_result_history_file"], headers, match_result_row)

def empty_results_csv(file_path):

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            header = f.readline()

        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(header)

        log.debug(f"File '{file_path}' svuotato con successo (header mantenuto).")

    except FileNotFoundError:
        log.error(f"Errore: Il file '{file_path}' non esiste.")
    except Exception as e:
        log.error(f"Si è verificato un errore: {e}")

def run_tournament(superplayers_file, mock=False):
    list_superplayers = load_superplayers_from_file(superplayers_file)
    delete_previous_logs(CONFIG["process_log_folder"])
    empty_results_csv(CONFIG["tournament_result_by_generation_file"])
    for sp1, sp2 in itertools.combinations(list_superplayers, 2):
        log.info(f"Match: {sp1.super_player_name} vs {sp2.super_player_name}")
        if not mock:
            match_bw_superplayers(sp1, sp2)
        store_result_of_match(sp1,sp2,mock)
    log.info("Torneo terminato")















