import json
from dataclasses import dataclass
from typing import Dict, Any, List
import subprocess
import os
import time
import sys

def run_server():
    # 1. Definisci le tue variabili (come faresti in Bash)
    MAIN_JAR = "target/tablut_benchmark_jar.jar"
    SERVER_CLASS = "it.unibo.ai.didattica.competition.tablut.server.Server"
    # Per i parametri, è meglio definirli come una lista di stringhe
    SERVER_PARAMETERS = ["-g", "-t", "2000"]
    LOGS_FOLDER = "target/logs"

    # 2. Prepara il percorso del file di log
    log_file_path = os.path.join(LOGS_FOLDER, "server.logs")

    # Assicurati che la cartella dei log esista
    os.makedirs(LOGS_FOLDER, exist_ok=True)

    # 3. Costruisci il comando come una lista di argomenti
    #    Questo è più sicuro che usare una stringa unica (evita shell injection)
    cmd = [
              "java",
              "-cp",
              MAIN_JAR,
              SERVER_CLASS
          ] + SERVER_PARAMETERS # Aggiunge i parametri alla fine

    print(f"Avvio del server... Log su: {log_file_path}") #TODO ATTENZIONE, IL FILE DI LOG CRESCE CON OGNI PARTITA

    try:
        # 4. Apri il file di log in modalità 'append' ('a')
        #    'with' si assicura che il file venga gestito correttamente
        with open(log_file_path, "a") as log_file:

            # 5. Avvia il processo
            process = subprocess.Popen(
                cmd,
                stdout=log_file,  # Redireziona stdout al file (come '>>')
                stderr=log_file   # Redireziona stderr allo stesso file (come '2>&1')
            )

        # Lo script Python continua immediatamente, senza aspettare la fine del jar
        print(f"Processo server avviato in background con PID: {process.pid}")

    except FileNotFoundError:
        print("ERRORE: Comando 'java' non trovato. Assicurati che sia nel PATH di sistema.")
    except Exception as e:
        print(f"Si è verificato un errore imprevisto: {e}")
    return process

def run_client(player):
    # 1. Definisci le tue variabili (come faresti in Bash)
    MAIN_JAR = "target/tablut_benchmark_jar.jar"
    SERVER_CLASS = "it.unibo.ai.didattica.competition.tablut.server.Server"
    # Per i parametri, è meglio definirli come una lista di stringhe
    LOGS_FOLDER = "target/logs"

    # 2. Prepara il percorso del file di log
    log_file_path = os.path.join(LOGS_FOLDER, "server.logs")

    # Assicurati che la cartella dei log esista
    os.makedirs(LOGS_FOLDER, exist_ok=True)

    # 3. Costruisci il comando come una lista di argomenti
    #    Questo è più sicuro che usare una stringa unica (evita shell injection)

    cmd = [
              "java",
              "-cp",
              MAIN_JAR,
              player.client_name
          ] + [player.role, '5', 'localhost', player.name]# Aggiunge i parametri alla fine

    print(f"Avvio del client... Log su: {log_file_path}")

    try:
        # 4. Apri il file di log in modalità 'append' ('a')
        #    'with' si assicura che il file venga gestito correttamente
        with open(log_file_path, "a") as log_file:

            # 5. Avvia il processo
            process = subprocess.Popen(
                cmd,
                stdout=log_file,  # Redireziona stdout al file (come '>>')
                stderr=log_file   # Redireziona stderr allo stesso file (come '2>&1')
            )

        # Lo script Python continua immediatamente, senza aspettare la fine del jar
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
    pass


def match_bw_superplayers(sp1,sp2):
    # white sp1 vs black sp2
    match_bw_players(sp1.player_w,sp2.player_b)

    # black sp1 vs white sp2
    match_bw_players(sp1.player_b,sp2.player_w)

    pass

# --- 1. Definizione delle Classi (come prima) ---
# Queste classi non cambiano. Il metodo from_dict
# è fondamentale e viene riutilizzato.

@dataclass
class Player:
    name: str
    client_name: str
    role: str
    heuristics: Dict[str, float]

    @staticmethod
    def from_dict(data: Dict[str, Any]) -> 'Player':
        """Crea un'istanza di Player da un dizionario (da JSON)."""
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
        """Crea un'istanza di Superplayer da un dizionario."""
        return Superplayer(
            super_player_name=data['superPlayerName'],
            player_w=Player.from_dict(data['playerW']),
            player_b=Player.from_dict(data['playerB'])
        )

# --- 2. NUOVA Funzione per leggere la LISTA da FILE ---

def load_superplayers_from_file(filename: str) -> List[Superplayer]:
    """
    Carica una lista di oggetti Superplayer da un file JSON.

    Args:
        filename (str): Il percorso del file JSON da leggere.

    Returns:
        List[Superplayer]: Una lista di istanze di Superplayer.
    """
    print(f"Tentativo di lettura dal file: {filename}")
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            # 1. Usa json.load() (senza 's') per leggere da un file
            # Questo 'data_list' sarà una lista di dizionari Python
            data_list = json.load(f)

        # 2. Usa una list comprehension per convertire ogni
        #    dizionario nella lista in un oggetto Superplayer
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

# --- 3. Esempio di Utilizzo ---

if __name__ == "__main__":

    # --- A. Creiamo un file JSON di esempio ---
    # (Questo simula un file JSON che già esiste)

    example_json_list_content = """
 [
  {
    "superPlayerName": "BaselineBlack_BaselineWhite_Superplayer",
    "playerW": {
      "name": "BaselineWhite", "clientName": "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline.BaselinePlayer", "role": "WHITE",
      "heuristics": {"controlloCentro": 0.75, "sicurezzaRe": 0.9}
    },
    "playerB": {   
      "name": "BaselineBlack", "clientName": "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline.BaselinePlayer", "role": "BLACK",
      "heuristics": {"controlloCentro": 0.72, "strutturaPedonale": 0.85}
    }
  },
  {
    "superPlayerName": "TavolettaBlack_TavolettaWhite_Superplayer",
    "playerW": {
      "name": "TavolettaWhite", "clientName": "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.tavoletta.TavolettaPlayer", "role": "WHITE",
      "heuristics": {"attivitaPezzi": 0.95}
    },
    "playerB": {
      "name": "TavolettaBlack", "clientName": "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.tavoletta.TavolettaPlayer", "role": "BLACK",
      "heuristics": {"difesaPura": 0.88}
    }
  }
]
    """

    nome_file_esempio = "partite.json"
    try:
        with open(nome_file_esempio, 'w', encoding='utf-8') as f:
            f.write(example_json_list_content)
        print(f"File di esempio '{nome_file_esempio}' creato con successo.")
    except IOError as e:
        print(f"Errore nella creazione del file di esempio: {e}")

    # --- B. Usiamo la nostra nuova funzione ---
    print("\n--- Inizio caricamento partite ---")
    list_superplayers = load_superplayers_from_file(nome_file_esempio)


    match_bw_superplayers(list_superplayers[0], list_superplayers[1])












