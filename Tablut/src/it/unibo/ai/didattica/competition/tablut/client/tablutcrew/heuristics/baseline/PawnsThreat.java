package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;

public class PawnsThreat extends Heuristic {
    // Theoretical bounds
    private static final Map<State.Turn, Double> MIN_VALUES = new HashMap<>();
    private static final Map<State.Turn, Double> MAX_VALUES = new HashMap<>();

    static {
        // Black perspective
        MIN_VALUES.put(State.Turn.BLACK, 0.0);
        MAX_VALUES.put(State.Turn.BLACK, 9.0);

        // White perspective
        MIN_VALUES.put(State.Turn.WHITE, 0.0);
        MAX_VALUES.put(State.Turn.WHITE, 16.0);
    }

    public PawnsThreat(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    @Override
    public double evaluateState(State state) {
        double maxValue = MAX_VALUES.get(currentPlayer);
        double minValue = MIN_VALUES.get(currentPlayer);

        double score = BaselineHeuristicsUtils.countCapturablePawns(state, currentPlayer);
        return normalize(score, minValue, maxValue);
    }
}
