import json
import copy
import random
import logging

import numpy as np
from config.logger import setup_logger, vmessage, verbose
from config.config_reader import CONFIG

import calculateElo as elo
import tournament_parallel as tournament


"""
SUPERPLAYER_BASE = {
    "superPlayerName": f"{id}",
    "playerW": {
        "name": f"{id}_White",
        "clientName": "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.DinamicPlayer",
        "role": "WHITE",
        "heuristics": {...}
    },
    "playerB": {
        "name": f"{id}_Black",
        "clientName": "it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.DinamicPlayer",
        "role": "BLACK",
        "heuristics": {...}
    }
}
"""

log = setup_logger(__name__)

base_superplayers_path = CONFIG["gen_alg_base_superplayers_path"]
superplayers_path = CONFIG["gen_alg_superplayers_path"]
final_population_path = CONFIG["gen_alg_final_population_path"]
tournament_result_history_file = CONFIG["tournament_result_history_file"]

last_id = CONFIG["gen_alg_popsize"] + 1 # GLOBAL SP ID COUNTER

def getSPName(individual):
    return individual["superPlayerName"]

def getSPHeuristicsWhite(individual):
    return individual["playerW"]["heuristics"]

def getSPHeuristicsBlack(individual):
    return individual["playerB"]["heuristics"]

def getSPHeuristics(individual): # NON USATA (potrebbe servire in caso di modifiche)
    return getSPHeuristicsWhite(individual) | getSPHeuristicsBlack(individual)

def getPopulationHeuristics(pop): # NON USATA (potrebbe servire in caso di modifiche)
    heuristics = {}

    # Salvo tutte le chiavi delle euristiche
    for SP in pop:
        heuristics |= getSPHeuristics(SP)

    # Imposto i valori delle euristiche a 0
    for k in heuristics:
        heuristics[k] = 0
    
    return heuristics

def getNewIndividualName(individual):
    global last_id

    name = 'SP'+str(last_id)
    
    individual["superPlayerName"] = name
    individual["playerW"]["name"] = name + "_White"
    individual["playerB"]["name"] = name + "_Black"

    last_id += 1


def mutate(individual, probability, current_gen=0, gens=1):
    prob = probability * (1 - float(current_gen) / gens) # Mutazione decrescente (annealing)

    W_H = getSPHeuristicsWhite(individual)
    B_H = getSPHeuristicsBlack(individual)

    for WH_key in W_H.keys():
        W_H[WH_key] += int(np.random.normal(0, prob * W_H[WH_key]))
        W_H[WH_key] = max(W_H[WH_key],0)

    for BH_key in B_H.keys():
        B_H[BH_key] += int(np.random.normal(0, prob * B_H[BH_key]))
        B_H[BH_key] = max(B_H[BH_key],0)

def crossover(parent1, parent2):
    # Copio il genitore, comprese le euristiche che andrò a modificare
    # Nota: uso parent1 ma andrebbe bene anche parent2, quello che ci interessa è la struttura
    individual = copy.deepcopy(parent1)

    # Modifico le informazioni specifiche
    getNewIndividualName(individual)

    # Individual 
    individual_WH = getSPHeuristicsWhite(individual)
    individual_BH = getSPHeuristicsBlack(individual)

    # Parent 1
    P1_WH = getSPHeuristicsWhite(parent1)
    P1_BH = getSPHeuristicsBlack(parent1)

    # Parent 2
    P2_WH = getSPHeuristicsWhite(parent2)
    P2_BH = getSPHeuristicsBlack(parent2)

    # Casualmente scelgo le euristiche tra i genitori
    # Nota: le chiavi dei genitori e dell'individuo sono uguali
    for WH_key in individual_WH.keys(): 
        individual_WH[WH_key] = random.choice([P1_WH[WH_key], P2_WH[WH_key]])

    for BH_key in individual_BH.keys():
        individual_BH[BH_key] = random.choice([P1_BH[BH_key], P2_BH[BH_key]])

    return individual


def generate_base(popsize, probability, verbose):
    global last_id

    pop = []

    with open(base_superplayers_path, "r") as f:
        base = json.load(f)

    pop += base

    if popsize != len(pop):
        last_id = len(pop) + 1

    remaining = popsize - len(base) # Numero di nuovi superplayer

    while remaining > 0:
        # Nota:
        # Il resto tra il numero dei rimanenti e la dimensione della base 
        # ci permette di avere una distribuzione equa dei superplayer base tra di superplayer mutati 
        base_SP = base[remaining % len(base)]  
        
        
        individual = copy.deepcopy(base_SP)

        getNewIndividualName(individual)

        mutate(
            individual=individual,
            probability=delta, 
            current_gen=0, 
            gens=1, 
        )

        pop.append(individual)


        remaining -= 1

    result = pop[:popsize]
    
    vmessage(f"Generate_base: {result}")
    
    return result

def getParent(pop, fitness_dict):
    # Creo un vettore di pesi con lo stesso ordine di pop (diverso da fitness_dict)
    weights = []

    for individual in pop:
        #Nota: uso dict.get perchè in fitness_dict non ci sono più tutti gli individui, ma solo i migliori
        weights.append(fitness_dict.get(getSPName(individual), 0)) 
         
    parent = random.choices(pop, weights=weights, k=1)[0]

    return parent

def generate_new_members(pop, fitness_dict, num_children, probability, current_gen, gens):
    new_members = []

    while len(new_members) < num_children:
        parent1 = getParent(pop, fitness_dict)
        parent2 = getParent(pop, fitness_dict)

        child = crossover(parent1, parent2)
        mutate(child, probability, current_gen, gens)

        new_members.append(child)

    return new_members

def select_best_withouth_ghosts(pop, fitness_dict, popsize):
    new_pop = []

    for player_name in fitness_dict:
        new_pop.append(getPlayerByName(player_name, pop))
        if len(new_pop) == popsize:
            break

    return new_pop

def select_best_with_ghosts(pop, fitness_dict, popsize, ghosts):
    new_pop = []
    ghosts_name = [getSPName(ghost) for ghost in ghosts]
    for player_name in fitness_dict:
        if player_name not in ghosts_name:
            new_pop.append(getPlayerByName(player_name, pop))
        if len(new_pop) == popsize:
            break

    return new_pop

def getPlayerByName(name, pop):
    for player in pop:
        if getSPName(player) == name:
            return player

def getFitness(pop, mock=False):
    # Salva popolazione
    with open(superplayers_path, "w") as f:
        json.dump(pop, f, indent=2)

    # Esegui il torneo
    tournament.run_tournament(superplayers_path, mock)

    # Calcola l'elo 
    fitness_dict = elo.calculate_points_ratings_sorted(CONFIG["tournament_result_by_generation_file"])

    # Restituisci la fitness {name : elo}
    return fitness_dict

def run_with_ghosts(current_best, gens, popsize, num_children, probability, verbose=False, mock=False):
    tournament.clear_old_results_csv(tournament_result_history_file) #SVUOTA il file dei risultati storici ogni volta che l'algoritmo viene attivato

    fitness_dict = getFitness(current_best, mock) # True = Mock
    ghosts = []
    for gen in range(gens):
        log.info(f"=== GENERATION {gen} ===")

        if verbose:
            best = max(
                current_best,
                key=lambda individual: fitness_dict[getSPName(individual)]
            )

            log.info(f"The best so far is {getSPName(best)} with fitness {fitness_dict[getSPName(best)]}")

        # Creo i nuovi membri
        new_members = generate_new_members(
            pop=current_best,
            fitness_dict=fitness_dict,
            num_children=num_children,
            probability=probability,
            current_gen=gen,
            gens=gens
        )

        # Unisco i nuovi membri ai precedenti
        combined = current_best + new_members
        vmessage(f"Current Population = {[getSPName(individual) for individual in combined]}")

        # Calcolo la fitness ({name : elo}) degli individui della popolazione
        # Nota: dato che sono già ordinati posso rimuovere gli "extra"

        if gen == 0:
            fitness_dict = getFitness(combined, mock) #alla prima iterazione non ci sono i fantasmi
        else:
            fitness_dict = getFitness(combined + ghosts, mock) #alla prima iterazione non ci sono i fantasmi
        vmessage(f"Fitness_dict (current generation) = {fitness_dict}")
        vmessage(f"Ordered Scoreboard (current generation) = {[name for name in fitness_dict]}")

        # Seleziono solo i migliori
        current_best = select_best_with_ghosts(combined, fitness_dict, popsize, ghosts)
        ghosts = select_ghosts(combined, current_best, gen, fitness_dict, ghosts)

        vmessage(f"Current best after select_best = {[getSPName(individual) for individual in current_best]}")
        vmessage(f"Current ghosts after select_ghosts = {[getSPName(individual) for individual in ghosts]}")

    return current_best
def run_without_ghosts(pop, gens, popsize, num_children, probability, verbose=False, mock=False):
    tournament.clear_old_results_csv(tournament_result_history_file) #SVUOTA il file dei risultati storici ogni volta che l'algoritmo viene attivato

    fitness_dict = getFitness(pop, mock) # True = Mock

    for gen in range(gens):
        log.info(f"=== GENERATION {gen} ===")

        if verbose:
            best = max(
                pop,
                key=lambda individual: fitness_dict[getSPName(individual)]
            )

            log.info(f"The best so far is {getSPName(best)} with fitness {fitness_dict[getSPName(best)]}")

        # Creo i nuovi membri
        new_members = generate_new_members(
            pop=pop,
            fitness_dict=fitness_dict,
            num_children=num_children,
            probability=probability,
            current_gen=gen,
            gens=gens
        )

        # Unisco i nuovi membri ai precedenti
        combined = pop + new_members
        vmessage(f"Combined Population = {combined}")
        vmessage(f"Combined Names = {[getSPName(individual) for individual in combined]}")

        # Calcolo la fitness ({name : points}) degli individui della popolazione
        # Nota: dato che sono già ordinati posso rimuovere gli "extra"

        fitness_dict = getFitness(combined, mock)
        vmessage(f"Fitness_dict (current generation) = {fitness_dict}")

        # Seleziono solo i migliori
        pop = select_best_withouth_ghosts(combined, fitness_dict, popsize)

        vmessage(f"Pop after select_best = {pop}", debug=True)
        vmessage(f"Best so far = {[getSPName(individual) for individual in pop]}")

    return pop

def run(current_best, gens, popsize, num_children, probability, verbose=False, mock=False, allow_ghosts=False):
    if allow_ghosts:
        return run_with_ghosts(current_best, gens, popsize, num_children, probability, verbose, mock)
    else:
        return run_without_ghosts(current_best, gens, popsize, num_children, probability, verbose, mock)


def select_ghosts(current_pop, current_best, gen, fitness_dict, current_ghosts):
    ghosts = []
    max_ghosts = min(CONFIG['gen_alg_max_ghosts'], len(current_pop) - len(current_best))
    current_best_names = [getSPName(i) for i in current_best]
    if gen == 0: #alla prima iterazione i fantasmi sono i perdenti
        current_pop_names = [getSPName(i) for i in current_pop]
        for player_name in current_pop_names:
            if player_name not in current_best_names:
                ghosts.append(getPlayerByName(player_name, current_pop))
            if len(ghosts) == max_ghosts:
                break
        for ghost in ghosts:
            ghost["superPlayerName"] += 'ghost'
            ghost["playerW"]["name"] += 'ghost'
            ghost["playerB"]["name"] += 'ghost'
        return ghosts
    else: #nelle altre iterazioni sono i caduti delle generazioni precedenti
        for player_name in fitness_dict:
            if player_name not in current_best_names:
                if player_name.endswith('ghost'):
                    ghosts.append(getPlayerByName(player_name, current_ghosts))
                else:
                    ghosts.append(getPlayerByName(player_name, current_pop))
            if len(ghosts) == max_ghosts:
                break
        for ghost in ghosts:
            if not ghost["superPlayerName"].endswith('ghost'):
                ghost["superPlayerName"] += 'ghost'
                ghost["playerW"]["name"] += 'ghost'
                ghost["playerB"]["name"] += 'ghost'
        return ghosts




if __name__ == "__main__":
    # Parametri dell'algoritmo genetico
    popsize = CONFIG["gen_alg_popsize"]
    num_children = CONFIG["gen_alg_num_children"]
    gens = CONFIG["gen_alg_num_generations"]

    # Parametri per la probabilità di mutazione
    delta = CONFIG["gen_alg_delta"]  # Variazione iniziale (possibilmente alta)
    sigma = CONFIG["gen_alg_sigma"] # Variazione durante le iterazioni (moderata)

    mock = CONFIG["gen_alg_mock"] #Per mockare le partite
    allow_ghosts = CONFIG["gen_alg_allow_ghosts"] #Per aggiungere i fantasmi ai tornei
    log.info("=== Starting ===")

    # Run GA
    final_pop = run(
        current_best=generate_base(popsize, delta, verbose),
        gens=gens,
        popsize=popsize,
        num_children=num_children,
        probability=sigma,
        verbose=verbose,
        mock=mock,
        allow_ghosts=allow_ghosts
    )

    log.info("=== FINAL pop ===")
    log.info(f"Final pop names = {[getSPName(player) for player in final_pop]}")
    
    vmessage(f"Final pop = {final_pop}")
    vmessage(f"Fitness dict (history of all players) = {elo.calculate_points_ratings_sorted(tournament_result_history_file)}")

    with open(final_population_path, "w") as f:
        json.dump(final_pop, f, indent=2)
