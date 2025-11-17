import json
import random
import subprocess
import numpy as np
import pprint

import calculateElo as ce

# file usati
base_SP_file    = "../../players/superplayers.json"
fitness_file    = "fitness.json" 
population_file = "population.json"
final_file      = "final_superplayer.json"



# Crea il dizionario con le euristiche di ogni SP 
def cleanSP(base_SP_list):
    heuristics = {}
    SP_cleaned = {}

    idSP = 1

    for SP in base_SP_list:
        heuristics |= {k: 0 for k in SP["playerW"]["heuristics"].keys()} # salvo le nuove euristiche del bianco
        heuristics |= {k: 0 for k in SP["playerB"]["heuristics"].keys()} # salvo le nuove euristiche del nero

        SP_cleaned[f"{idSP}"] = SP["playerW"]["heuristics"] | SP["playerB"]["heuristics"]

        idSP += 1

    for i in range(1, len(base_SP_list) + 1):
        SP_cleaned[f"{i}"] = heuristics | SP_cleaned[f"{i}"] # Imposto a 0 le euristiche non presenti originariamente nei superplayer  

    return SP_cleaned


# Mutazione decrescente (annealing)
# Serve per esplorare inizialmente soluzioni abbastanza differenti tra loro, 
# per poi passare piano piano a soluzioni più precise ed ottimali 
# Nota: se non si passa current_gen la mutazione rimane quella di base (sigma)
def mutation_probability(probability, current_gen=0, gens=1):
    return probability * (1 - current_gen / gens)


# Funzione di mutazione proporzionale (mantieniamo la scala)
def mutate(SP, probability, popsize, current_gen=0, gens=1):    
    new[idSP] = {}

    idSP = list(new.keys())[0]

    mp = mutation_probability(probability, current_gen, gens)

    for key, val in list(new.values())[0].items():
        if val == 0:
            val = 0.01

        new[idSP][key] = round(val + np.random.normal(0, mp * val), 3) # v + 0 + deviazione standard (mp * v) casuale

        print(new[key])

    return new


# inizializza popolazione clonando i semi
def create_random_population(SP_list, popsize, delta):
    pop = []

    while len(pop) < popsize:
        idSP, heuristics = random.choice(list(SP_list.items()))
        pop.append(mutate({idSP: heuristics}, probability=delta, popsize=popsize))
    
    return pop


# Valuta la fitness chiamando i tuoi script Java/Bash
def get_fitness(pop):
    with open(population_file, "w") as f:
        json.dump(pop, f, indent=2)

    # esegue il tuo torneo esterno
    result = ce.return_fitness(pop)

    # Il torneo deve generare un file fitness.json
    with open(fitness_file) as f:
        fitness = json.load(f)

    return fitness     # lista di numeri, ordinata come pop


# Evoluzione vera e propri
def run(pop, popsize, gens, sigma):
    for gen in range(gens):
        print(f"--- GENERAZIONE {gen} ---")

        # valuta la popolazione (via script Java/Bash)
        fitness = get_fitness(pop)

        # ordina per fitness (max → migliore)
        ranked = sorted(zip(pop, fitness), key=lambda x: x[1], reverse=True)

        elities = ranked[:popsize]

        print("Migliore:", ranked[0][1])

        # rigenera popolazione con mutazioni degli élite
        new_pop = []
        while len(new_pop) < popsize:
            base = random.choice(elites)
            new_pop.append(mutate(base, sigma, popsize=popsize, current_gen=gen, gens=gens))

        pop = new_pop

    return pop



if __name__ == "__main__":

    # parametri dell'algoritmo genetico
    popsize = 3
    gens = 50

    # Probabilità di mutazione
    delta = 0.2 # Inizializzazione
    sigma = 0.07 # Ogni iterazione



    with open(base_SP_file) as f:
        base_SP_list = json.load(f)

    cleaned_SP_list = cleanSP(base_SP_list=base_SP_list)

    pop = create_random_population(SP_list=cleaned_SP_list, popsize=popsize, delta=delta)


    final_population = run(originalSP, popsize=popsize, gens=gens, sigma=sigma)

    with open(final_population_filename, "w") as f:
        json.dump(final_population, f, indent=2)

