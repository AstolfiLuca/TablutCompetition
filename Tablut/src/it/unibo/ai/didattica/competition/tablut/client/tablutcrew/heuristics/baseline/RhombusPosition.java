package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;

public class RhombusPosition extends Heuristic {
    // Theoretical bounds
    private static final Map<State.Turn, Double> MIN_VALUES = new HashMap<>();
    private static final Map<State.Turn, Double> MAX_VALUES = new HashMap<>();

    static {
        // Black perspective: score = black - (white + king)
        MIN_VALUES.put(State.Turn.BLACK, 0.0);
        MAX_VALUES.put(State.Turn.BLACK, 8.0);

        // White perspective: score = (white + king) - black
        MIN_VALUES.put(State.Turn.WHITE, 0.0);
        MAX_VALUES.put(State.Turn.WHITE, 8.0);
    }

    // Matrix of favourite black positions in the initial stages to block the escape ways
    private final int[][] RHOMBUS_POSITIONS = {
                  {1,2},       {1,6},
            {2,1},                   {2,7},

            {6,1},                   {6,7},
                  {7,2},       {7,6}
    };

    public RhombusPosition(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    /**
     * @return the number of black pawns on rhombus configuration relatively to the maximum
     */
    @Override
    public double evaluateState(State state) {
        int count = 0;

        for (int[] position : RHOMBUS_POSITIONS) {
            if (state.getPawn(position[0], position[1]).equalsPawn(State.Pawn.BLACK.toString())) {
                count++;
            }
        }
        double minValue = MIN_VALUES.get(currentPlayer);
        double maxValue = MAX_VALUES.get(currentPlayer);

        double score = normalize(count, minValue, maxValue);
        return currentPlayer == State.Turn.BLACK ? score : -score;
    }
}
