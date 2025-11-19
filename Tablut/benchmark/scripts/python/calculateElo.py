import csv
import math
from config.config_reader import CONFIG
from config.logger import setup_logger

log = setup_logger(__name__)

# Punteggio ELO iniziale per ogni nuovo Superplayer
STARTING_ELO = CONFIG["starting_elo"]

# Fattore K: determina quanto velocemente i punteggi ELO cambiano.
# Un K più alto significa cambiamenti più rapidi (più volatile).
K_FACTOR = CONFIG["k_factor"]

def calculate_elo_ratings(csv_file_path):

    #Il CSV deve avere il formato:
    #[Timestamp, SuperPlayer_1, SuperPlayer_2, Punteggio_SP1, Punteggio_SP2]

    elo_ratings = {}

    try:
        with open(csv_file_path, mode='r', encoding='utf-8') as file:
            reader = csv.reader(file)

            # Salta l'intestazione (header) del CSV, se presente
            try:
                next(reader)
            except StopIteration:
                log.error("Errore: il file CSV è vuoto.")
                return {}

            # Processa ogni riga in ordine cronologico
            for row in reader:
                if not row:  # Salta righe vuote
                    continue
                try:
                    timestamp, sp1_name, sp2_name, score_sp1_str, score_sp2_str = row
                    score_sp1 = float(score_sp1_str)
                    score_sp2 = float(score_sp2_str)

                    # Se un player è nuovo, assegna il punteggio ELO iniziale
                    if sp1_name not in elo_ratings:
                        elo_ratings[sp1_name] = STARTING_ELO
                    if sp2_name not in elo_ratings:
                        elo_ratings[sp2_name] = STARTING_ELO

                    # Ottieni i rating ELO correnti
                    r1 = elo_ratings[sp1_name]
                    r2 = elo_ratings[sp2_name]


                    # Calcola la probabilità di vittoria attesa (E) per un gioco da 1 punto
                    # Questa è la formula ELO standard
                    e1_prob = 1 / (1 + math.pow(10, (r2 - r1) / 400))
                    e2_prob = 1 - e1_prob

                    # Adatta il punteggio atteso al sistema (scala 0-2)
                    # Il punteggio atteso è semplicemente la probabilità di vincere * il punteggio massimo
                    expected_score_1 = e1_prob * 2
                    expected_score_2 = e2_prob * 2

                    # Calcola il nuovo ELO usando la formula di aggiornamento
                    # NuovoRating = VecchioRating + K * (PunteggioReale - PunteggioAtteso)
                    new_r1 = r1 + K_FACTOR * (score_sp1 - expected_score_1)
                    new_r2 = r2 + K_FACTOR * (score_sp2 - expected_score_2)


                    elo_ratings[sp1_name] = new_r1
                    elo_ratings[sp2_name] = new_r2

                except ValueError:
                    log.error(f"Errore nel formato dati della riga: {row}. Riga saltata.")
                except Exception as e:
                    log.error(f"Errore durante l'elaborazione della riga {row}: {e}")

    except FileNotFoundError:
        log.error(f"Errore: File non trovato a '{csv_file_path}'")
        return {}
    except Exception as e:
        log.error(f"Errore imprevisto nell'apertura del file: {e}")
        return {}

    return elo_ratings

def calculate_elo_ratings_sorted(file_path):
    calculate_elo_ratings(file_path)
    final_ratings = calculate_elo_ratings(file_path)
    final_ratings_dict = {}
    if final_ratings:
        # Ordina i risultati per punteggio ELO (dal più alto al più basso)
        # per una classifica leggibile
        sorted_ratings = sorted(final_ratings.items(), key=lambda item: item[1], reverse=True)

        for player, rating in sorted_ratings:
            log.debug(f"ELO di {player}: {rating:.0f}")
            final_ratings_dict[player] = rating

    return final_ratings_dict


if __name__ == "__main__":
    print(calculate_elo_ratings_sorted(CONFIG["tournament_result_file"]))
