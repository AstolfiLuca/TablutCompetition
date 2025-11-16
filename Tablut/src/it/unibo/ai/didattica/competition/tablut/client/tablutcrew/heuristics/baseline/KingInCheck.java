package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class KingInCheck extends Heuristic {

    public KingInCheck(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    @Override
    public double evaluateState(State state) {
        int kingRow = boardState.getKingRow();
        int kingCol = boardState.getKingCol();

        int kingInCheck = BaselineHeuristicsUtils.isCapturableKing(state.getBoard(), kingRow, kingCol) ? 1 : 0;
        return currentPlayer == State.Turn.BLACK ? (10 * kingInCheck) : (-10 * kingInCheck);
    }
}
