package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.search;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
 import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
 import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.BlackBaselineHeuristics;
 import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.WhiteBaselineHeuristics;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.Map;

public class BaselineSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {
    protected Map<String, Double> weights = null;

    public BaselineSearch(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time, Map<String, Double> weights) {
        super(game, utilMin, utilMax, time);
        this.weights = weights;
    }

    @Override
    protected double eval(State state, State.Turn player){
        // Compute the state value and set heuristicsEnabled
        //we call the eval method from the superclass because this sets to true the flag heuristicEvaluationUsed
        double result = super.eval(state, player);
        if (game.isTerminal(state)) {
            return result;
        }

        Heuristic heuristics = player.equals(State.Turn.WHITE) ? new WhiteBaselineHeuristics(state, player, weights) : new BlackBaselineHeuristics(state, player, weights);

        // Return heuristic value for the given state
        return heuristics.evaluateState(state);
    }




}
