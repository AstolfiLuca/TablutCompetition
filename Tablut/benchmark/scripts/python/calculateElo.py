import csv
import math

# --- Parametri del Sistema ELO ---

# Punteggio ELO iniziale per ogni nuovo Superplayer
STARTING_ELO = 1500

# Fattore K: determina quanto velocemente i punteggi ELO cambiano.
# Un K più alto significa cambiamenti più rapidi (più volatile).
# 32 è un valore standard.
K_FACTOR = 32

def calculate_elo_ratings(csv_file_path):
    """
    Calcola i punteggi ELO da un file CSV di risultati di match.

    Il CSV deve avere il formato:
    [Timestamp, SuperPlayer_1, SuperPlayer_2, Punteggio_SP1, Punteggio_SP2]
    """

    # Un dizionario per memorizzare il punteggio ELO corrente di ogni player
    # es: {'PlayerA': 1510, 'PlayerB': 1490}
    elo_ratings = {}

    try:
        with open(csv_file_path, mode='r', encoding='utf-8') as file:
            reader = csv.reader(file)

            # Salta l'intestazione (header) del CSV, se presente
            try:
                next(reader)
            except StopIteration:
                print("Errore: il file CSV è vuoto.")
                return {}

            # Processa ogni riga (Mini-Match) in ordine cronologico
            for row in reader:
                if not row:  # Salta righe vuote
                    continue

                try:
                    # Estrai i dati dalla riga del CSV
                    timestamp, sp1_name, sp2_name, score_sp1_str, score_sp2_str = row

                    # Converte i punteggi in float
                    score_sp1 = float(score_sp1_str)
                    score_sp2 = float(score_sp2_str)

                    # --- Inizializzazione ELO ---
                    # Se un player è nuovo, assegna il punteggio ELO iniziale
                    if sp1_name not in elo_ratings:
                        elo_ratings[sp1_name] = STARTING_ELO
                    if sp2_name not in elo_ratings:
                        elo_ratings[sp2_name] = STARTING_ELO

                    # Ottieni i rating ELO correnti
                    r1 = elo_ratings[sp1_name]
                    r2 = elo_ratings[sp2_name]

                    # --- Calcolo ELO ---

                    # 1. Calcola la probabilità di vittoria attesa (E) per un gioco da 1 punto
                    # Questa è la formula ELO standard
                    e1_prob = 1 / (1 + math.pow(10, (r2 - r1) / 400))
                    e2_prob = 1 - e1_prob

                    # 2. Adatta il punteggio atteso al tuo sistema (scala 0-2)
                    # Il punteggio atteso è semplicemente la probabilità di vincere * il punteggio massimo
                    expected_score_1 = e1_prob * 2
                    expected_score_2 = e2_prob * 2

                    # 3. Calcola il nuovo ELO usando la formula di aggiornamento
                    # NuovoRating = VecchioRating + K * (PunteggioReale - PunteggioAtteso)
                    new_r1 = r1 + K_FACTOR * (score_sp1 - expected_score_1)
                    new_r2 = r2 + K_FACTOR * (score_sp2 - expected_score_2)

                    # 4. Aggiorna i punteggi ELO nel dizionario
                    elo_ratings[sp1_name] = new_r1
                    elo_ratings[sp2_name] = new_r2

                except ValueError:
                    print(f"Errore nel formato dati della riga: {row}. Riga saltata.")
                except Exception as e:
                    print(f"Errore durante l'elaborazione della riga {row}: {e}")

    except FileNotFoundError:
        print(f"Errore: File non trovato a '{csv_file_path}'")
        return {}
    except Exception as e:
        print(f"Errore imprevisto nell'apertura del file: {e}")
        return {}

    return elo_ratings

# --- Esecuzione ---

if __name__ == "__main__":
    # Sostituisci 'tuoi_match.csv' con il percorso reale del tuo file
    file_path = 'match_results.csv'

    final_ratings = calculate_elo_ratings(file_path)

    if final_ratings:
        print("--- Punteggi ELO Finali ---")

        # Ordina i risultati per punteggio ELO (dal più alto al più basso)
        # per una classifica leggibile
        sorted_ratings = sorted(final_ratings.items(), key=lambda item: item[1], reverse=True)

        for player, rating in sorted_ratings:
            # :.0f formatta il numero come intero (es. 1500 invece di 1500.123)
            print(f"{player}: {rating:.0f}")