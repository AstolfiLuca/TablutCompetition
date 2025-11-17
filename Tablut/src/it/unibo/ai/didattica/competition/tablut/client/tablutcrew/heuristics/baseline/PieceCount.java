package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;

public class PieceCount extends Heuristic {
    // Theoretical bounds
    private static final Map<State.Turn, Double> MIN_VALUES = new HashMap<>();
    private static final Map<State.Turn, Double> MAX_VALUES = new HashMap<>();

    static {
        // Black perspective: score = black - (white + king)
        MIN_VALUES.put(State.Turn.BLACK, -9.0);
        MAX_VALUES.put(State.Turn.BLACK, 16.0);

        // White perspective: score = (white + king) - black
        MIN_VALUES.put(State.Turn.WHITE, -16.0);
        MAX_VALUES.put(State.Turn.WHITE, 9.0);
    }

    public PieceCount(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    @Override
    public double evaluateState(State state) {
        int whitePawns = boardState.getWhitePawns();
        int blackPawns = boardState.getBlackPawns();

        double minValue = MIN_VALUES.get(currentPlayer);
        double maxValue = MAX_VALUES.get(currentPlayer);

        double score = currentPlayer == State.Turn.WHITE ? ((whitePawns + 1) - blackPawns) : (blackPawns - (whitePawns + 1));
        return normalize(score, minValue, maxValue);
    }
}
