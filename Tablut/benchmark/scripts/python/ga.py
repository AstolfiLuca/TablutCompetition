import json
import copy
import random
import logging

import numpy as np
from config.logger import setup_logger
from config.config_reader import CONFIG

import calculateElo as elo
import tournament as tournament


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


def  mutate(individual, probability, current_gen=0, gens=1): #TODO funziona anche senza copy?
    prob = probability * (1 - float(current_gen) / gens) # Mutazione decrescente (annealing)

    W_H = getSPHeuristicsWhite(individual)
    B_H = getSPHeuristicsBlack(individual)

    for WH_key in W_H.keys():
        W_H[WH_key] += int(np.random.normal(0, prob * W_H[WH_key]))

    for BH_key in B_H.keys():
        B_H[BH_key] += int(np.random.normal(0, prob * B_H[BH_key]))

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


def generate_base(popsize, probability):
    global last_id

    pop = []

    with open(base_superplayers_path, "r") as f:
        base = json.load(f)

    pop += base

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
    log.info(f"Generate_base: {result}")
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

def select_best(pop, fitness_dict, popsize):
    new_pop = []
    names_of_alive_players = [getSPName(player) for player in pop]


    for name in fitness_dict:
        if name in names_of_alive_players:
            new_pop.append(getPlayerByName(name, pop))
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
    tournament.run_tournament(CONFIG["superplayers_file"], mock)

    # Calcola l'elo 
    fitness_dict = elo.calculate_elo_ratings_sorted(CONFIG["tournament_result_file"])

    # Restituisci la fitness {name : elo}
    return fitness_dict

import os

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



def run(pop, gens, popsize, num_children, probability, verbose=False):
    empty_results_csv(CONFIG["tournament_result_file"]) #SVUOTA il file dei risultati ogni volta che l'algoritmo viene attivato
    fitness_dict = getFitness(pop, True) # True = Mock

    for gen in range(gens):
        if verbose:
            log.info(f"=== GENERATION {gen} ===")

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
        log.info(f"Combined Population = {combined}")
        log.info(f"Combined Names = {[getSPName(individual) for individual in combined]}")

        # Calcolo la fitness ({name : elo}) degli individui della popolazione 
        # Nota: dato che sono già ordinati posso rimuovere gli "extra"

        fitness_dict = getFitness(combined, True)
        log.info(f"Fitness_dict (all players) = {fitness_dict}")

        # Seleziono solo i migliori
        pop = select_best(combined, fitness_dict, popsize)
        log.info(f"Pop after select_best = {pop}")
        log.info(f"Names after select_best = {[getSPName(individual) for individual in pop]}")

    return pop


if __name__ == "__main__":
    # Attivazione logging
    verbose = True

    # Parametri dell'algoritmo genetico
    popsize = CONFIG["gen_alg_popsize"]
    num_children = CONFIG["gen_alg_num_children"]
    gens = CONFIG["gen_alg_num_generations"]

    # Parametri per la probabilità di mutazione
    delta = CONFIG["gen_alg_delta"]  # Variazione iniziale (possibilmente alta)
    sigma = CONFIG["gen_alg_sigma"] # Variazione durante le iterazioni (moderata)

    # Run GA
    final_pop = run(
        pop=generate_base(popsize, delta),
        gens=gens,
        popsize=popsize,
        num_children=num_children,
        probability=sigma,
        verbose=verbose
    )

    if verbose:
        log.info("=== FINAL pop ===")
        log.info(f"Final pop = {final_pop}")
        log.info(f"Final pop names = {[getSPName(player) for player in final_pop]}")

    with open(final_population_path, "w") as f:
        json.dump(final_pop, f, indent=2)
