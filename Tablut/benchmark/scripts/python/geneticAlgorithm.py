import tournament as tournament
import calculateElo as elo
from config.config_reader import CONFIG
from config.logger import setup_logger

log = setup_logger(__name__)

def getFitness():
    log.info("Avvio il torneo tra superplayer")
    tournament.run_tournament(CONFIG["superplayers_file"])
    return elo.calculate_elo_ratings_sorted(CONFIG["tournament_result_file"])


if __name__ == "__main__":
    #Mock
    #chiamata alla fitness function

    fitness = getFitness()
    print(fitness)