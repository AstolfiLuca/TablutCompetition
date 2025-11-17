package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;

public class PawnsSafety extends Heuristic {
    // Theoretical bounds
    private static final Map<State.Turn, Double> MIN_VALUES = new HashMap<>();
    private static final Map<State.Turn, Double> MAX_VALUES = new HashMap<>();

    static {
        // Black perspective
        MIN_VALUES.put(State.Turn.BLACK, 0.0);
        MAX_VALUES.put(State.Turn.BLACK, 16.0);

        // White perspective
        MIN_VALUES.put(State.Turn.WHITE, 0.0);
        MAX_VALUES.put(State.Turn.WHITE, 9.0);
    }

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

        double minValue = MIN_VALUES.get(currentPlayer);
        double maxValue = MAX_VALUES.get(currentPlayer);

        double score = pawnsAlive - BaselineHeuristicsUtils.countCapturablePawns(state, enemy);
        return normalize(score, minValue, maxValue);
    }
}
