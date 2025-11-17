package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;

public class KingInCheck extends Heuristic {
    // Theoretical bounds
    private static final Map<State.Turn, Double> MIN_VALUES = new HashMap<>();
    private static final Map<State.Turn, Double> MAX_VALUES = new HashMap<>();

    static {
        // Black perspective
        MIN_VALUES.put(State.Turn.BLACK, 0.0);
        MAX_VALUES.put(State.Turn.BLACK, 10.0);

        // White perspective
        MIN_VALUES.put(State.Turn.WHITE, -10.0);
        MAX_VALUES.put(State.Turn.WHITE, 0.0);
    }

    public KingInCheck(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    @Override
    public double evaluateState(State state) {
        int kingRow = boardState.getKingRow();
        int kingCol = boardState.getKingCol();

        int kingInCheck = BaselineHeuristicsUtils.isCapturableKing(state.getBoard(), kingRow, kingCol) ? 1 : 0;
        double score = currentPlayer == State.Turn.BLACK ? (10 * kingInCheck) : (-10 * kingInCheck);
        double minValue = MIN_VALUES.get(currentPlayer);
        double maxValue = MAX_VALUES.get(currentPlayer);

        return normalize(score, minValue, maxValue);
    }
}
