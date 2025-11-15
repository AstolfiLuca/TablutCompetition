import tournament as tournament
import calculateElo as elo

def getFitness(superplayers_file):
    tournament_results_file = tournament.run_tournament(superplayers_file)
    elo_sorted_ratings = elo.calculate_elo_ratings_sorted(tournament_results_file)
    return elo_sorted_ratings


if __name__ == "__main__":
    #Mock
    #chiamata alla fitness function
    superplayers_file = "../../players/superplayers.json"
    fitness = getFitness(superplayers_file)
    print(fitness)