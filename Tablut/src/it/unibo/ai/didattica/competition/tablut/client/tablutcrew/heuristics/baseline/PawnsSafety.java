package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class PawnsSafety extends Heuristic {

    public PawnsSafety(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    /**
     * Returns a score that describes the safety of the pawns of
     * the given player, according to the state of the board.
     *
     * The safety is computed based on the alive pawns and the ones that could be captured.
     */
    @Override
    public double evaluateState(State state) {
        int blackPawns = boardState.getBlackPawns();
        int whitePawns = boardState.getWhitePawns();

        State.Turn enemy = currentPlayer == State.Turn.BLACK ? State.Turn.WHITE : State.Turn.BLACK;
        int pawnsAlive = currentPlayer == State.Turn.BLACK ? blackPawns : whitePawns;

        return (double) (pawnsAlive - BaselineHeuristicsUtils.countCapturablePawns(state, enemy)) / pawnsAlive;
    }
}
