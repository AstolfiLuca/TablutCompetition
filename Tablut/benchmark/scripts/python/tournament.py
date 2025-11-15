import csv
import glob
import itertools
import json
from dataclasses import dataclass
from datetime import datetime
from typing import Dict, Any, List
import subprocess
import os
import time
from config.config_reader import CONFIG

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

    print(f"Avvio del server... Log su: {log_file_path}") #TODO ATTENZIONE, IL FILE DI LOG CRESCE CON OGNI PARTITA

    try:
        #Apri il file di log in modalità 'append' ('a')
        with open(log_file_path, "a") as log_file:

            process = subprocess.Popen(
                cmd,
                stdout=log_file,  # Redireziona stdout al file (come '>>')
                stderr=log_file   # Redireziona stderr allo stesso file (come '2>&1')
            )

        print(f"Processo server avviato in background con PID: {process.pid}")

    except FileNotFoundError:
        print("ERRORE: Comando 'java' non trovato. Assicurati che sia nel PATH di sistema.")
    except Exception as e:
        print(f"Si è verificato un errore imprevisto: {e}")
    return process

def run_client(player):

    process_log_folder = CONFIG["process_log_folder"]
    log_file_path = os.path.join(process_log_folder, player.name+".logs")
    os.makedirs(process_log_folder, exist_ok=True)

    cmd = [
              "java",
              "-cp",
              CONFIG["client"]["jar"],
              player.client_name
          ] + [player.role, str(CONFIG["client"]["timeout"]), CONFIG["client"]["server_ip"], player.name] # Aggiunge i parametri alla fine

    print(f"Avvio del client... Log su: {log_file_path}")

    try:
        # Apri il file di log in modalità 'append' ('a')
        with open(log_file_path, "a") as log_file:

            # 5. Avvia il processo
            process = subprocess.Popen(
                cmd,
                stdout=log_file,  # Redireziona stdout al file (come '>>')
                stderr=log_file   # Redireziona stderr allo stesso file (come '2>&1')
            )

        print(f"Processo Client avviato in background con PID: {process.pid}")

    except FileNotFoundError:
        print("ERRORE: Comando 'java' non trovato. Assicurati che sia nel PATH di sistema.")
    except Exception as e:
        print(f"Si è verificato un errore imprevisto: {e}")
    return process


def match_bw_players(p1,p2):
    #start server
    server_process = run_server()
    time.sleep(1)
    #run client p1 with heuristics in input
    client1_process = run_client(p1)
    time.sleep(1)
    #run client p2 with heuristics in input
    client2_process = run_client(p2)
    time.sleep(1)
    processes = [server_process, client1_process, client2_process]
    for process in processes:
        process.wait()
        print(f"Processo (PID: {process.pid}) ha terminato.")
    print("Tutti i processi del match hanno terminato")



def match_bw_superplayers(sp1,sp2):
    # white sp1 vs black sp2
    match_bw_players(sp1.player_w,sp2.player_b)

    # black sp1 vs white sp2
    match_bw_players(sp1.player_b,sp2.player_w)






def load_superplayers_from_file(filename):

    print(f"Tentativo di lettura dal file: {filename}")
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            data_list = json.load(f)

        return [Superplayer.from_dict(item) for item in data_list]

    except FileNotFoundError:
        print(f"ERRORE: File non trovato a '{filename}'")
        return []
    except json.JSONDecodeError:
        print(f"ERRORE: Il file '{filename}' non è un JSON valido o è vuoto.")
        return []
    except KeyError as e:
        print(f"ERRORE: Manca una chiave nel JSON: {e}. Controlla la mappatura.")
        return []
    except Exception as e:
        print(f"ERRORE sconosciuto durante la lettura: {e}")
        return []



def delete_previous_logs(folder):
    if not os.path.isdir(folder):
        print(f"Errore: Il percorso '{folder}' non è una cartella valida.")
        return
    print(f"Pulizia della cartella: {folder}...")
    for nome_elemento in os.listdir(folder):
        percorso_completo = os.path.join(folder, nome_elemento)
        try:
            if os.path.isfile(percorso_completo):
                os.remove(percorso_completo)

        except Exception as e:
            print(f"Errore durante la cancellazione di {nome_elemento}: {e}")

def lookup_game_result(player_w, player_b):
    player_w_name = player_w.name
    player_b_name = player_b.name
    game_log_file_name = '_'+player_w_name + '_vs_' + player_b_name + '_'
    pattern = os.path.join("./logs", f"{game_log_file_name}*")

    files_found = glob.glob(pattern)
    filename = None
    try:
        filename = files_found[0]
        print(f"Log del game salvato in {filename}")
    except:
        print("[ERROR] Impossibile trovare il file di log del game")
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
        print(f"Si è verificato un errore durante la lettura: {e}")
        return None

    return last_row


def write_on_csv(filename, headers, row):

    file_exists = os.path.exists(filename)
    with open(filename, mode='a', newline='', encoding='utf-8') as file_csv:
        writer = csv.writer(file_csv, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

        if not file_exists:
            writer.writerow(headers)

        writer.writerow(row)



def store_result_of_match(sp1,sp2):
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
        raise Exception("Impossibile leggere il risultato")
    if sp1_black_vs_sp2_white == 'WW':
        sp1_points += 0
    elif sp1_black_vs_sp2_white == 'D':
        sp1_points += 0.5
    elif sp1_black_vs_sp2_white == 'BW':
        sp1_points += 1
    else:
        raise Exception("Impossibile leggere il risultato")
    sp2_points = 2-sp1_points
    format = "%d-%m-%Y %H:%M"
    timestamp = datetime.now().strftime(format)
    match_result_row = (timestamp, sp1.super_player_name, sp2.super_player_name, sp1_points,sp2_points)
    headers = ["Timestamp", "SuperPlayer_1", "SuperPlayer_2", "Punteggio_SP1", "Punteggio_SP2"]

    write_on_csv(CONFIG["tournament_result_file"], headers, match_result_row)



def run_tournament(superplayers_file):
    list_superplayers = load_superplayers_from_file(superplayers_file)
    delete_previous_logs(CONFIG["process_log_folder"])
    for sp1, sp2 in itertools.combinations(list_superplayers, 2):
        print(f"Partita: {sp1.super_player_name} vs {sp2.super_player_name}")
        match_bw_superplayers(sp1, sp2)
        store_result_of_match(sp1,sp2)















