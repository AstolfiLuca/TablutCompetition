package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class PieceCount extends Heuristic {

    public PieceCount(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    @Override
    public double evaluateState(State state) {
        int whitePawns = boardState.getWhitePawns();
        int blackPawns = boardState.getBlackPawns();

        return currentPlayer == State.Turn.WHITE ? ((whitePawns + 1) - blackPawns) : (blackPawns - (whitePawns + 1));
    }
}
