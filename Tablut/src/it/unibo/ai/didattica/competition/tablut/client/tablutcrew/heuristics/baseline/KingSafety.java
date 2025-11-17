package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;

public class KingSafety extends Heuristic {
    // Theoretical bounds
    private static final Map<State.Turn, Double> MIN_VALUES = new HashMap<>();
    private static final Map<State.Turn, Double> MAX_VALUES = new HashMap<>();

    static {
        // Black perspective
        MIN_VALUES.put(State.Turn.BLACK, -4.0);
        MAX_VALUES.put(State.Turn.BLACK, 0.0);

        // White perspective
        MIN_VALUES.put(State.Turn.WHITE, 0.0);
        MAX_VALUES.put(State.Turn.WHITE, 4.0);
    }

    public KingSafety(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }


    @Override
    public double evaluateState(State state) {
        int kingRow = boardState.getKingRow();
        int kingCol = boardState.getKingCol();
        State.Pawn[][] board = state.getBoard();

        int adjacentDefenders = 0;

        int[] dr = {-1, 1, 0, 0}; // N, S, E, W
        int[] dc = {0, 0, 1, -1};

        for (int i = 0; i < 4; i++) {
            int nr = kingRow + dr[i];
            int nc = kingCol + dc[i];
            // Check if the adjacent square is on the board
            if (nr >= 0 && nr < board.length && nc >= 0 && nc < board.length)
                if (board[nr][nc].belongsTo(State.Turn.WHITE))
                    adjacentDefenders++;
        }

        double minValue = MIN_VALUES.get(currentPlayer);
        double maxValue = MAX_VALUES.get(currentPlayer);

        double score = currentPlayer == State.Turn.WHITE ? adjacentDefenders : -adjacentDefenders;
        return normalize(score, minValue, maxValue);
    }
}
