package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public abstract class Heuristic {
    public BaselineHeuristicsUtils.BoardState boardState;
    public State.Turn currentPlayer;

    public Heuristic(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        this.boardState = boardState;
        this.currentPlayer = currentPlayer;
    }

    public abstract double evaluateState(State state);
}
