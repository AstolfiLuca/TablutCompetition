package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.search;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.BlackBaselineHeuristics;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.WhiteBaselineHeuristics;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class TablutCrewSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {
    public TablutCrewSearch(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    @Override
    protected double eval(State state, State.Turn player){
        // Compute the state value and set heuristicsEnabled
        double result = super.eval(state, player);
        if (game.isTerminal(state)) {
            return result;
        }

        Heuristic heuristics = player.equals(State.Turn.WHITE) ? new WhiteBaselineHeuristics(state) : new BlackBaselineHeuristics(state);
        // Return heuristic value for the given state
        return heuristics.evaluateState();
    }




}
