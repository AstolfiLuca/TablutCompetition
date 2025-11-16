package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.List;

public class KingLineEvaluation extends Heuristic {

    public KingLineEvaluation(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    public double evaluateState(State state) {
        int kingRow = boardState.getKingRow();
        int kingCol = boardState.getKingCol();
        List<Integer[]> blackPositions = boardState.getBlackPositions();
        List<Integer[]> whitePositions = boardState.getWhitePositions();

        double rowScore = evaluateKingLine(state.getBoard(), kingRow, kingCol, blackPositions, whitePositions, currentPlayer, true);   // row
        double colScore = evaluateKingLine(state.getBoard(), kingRow, kingCol, blackPositions, whitePositions, currentPlayer, false);  // column

        return rowScore + colScore;
    }

    /**
     * Evaluates threats and bonuses along one direction (row or column)
     * */
    private double evaluateKingLine(State.Pawn[][] board, int kingRow, int kingCol, List<Integer[]> blackPositions, List<Integer[]> whitePositions, State.Turn currentPlayer, boolean isRow) {
        double score = 0;
        for (int i = 0; i < 9; i++) {
            int r = isRow ? kingRow : i;
            int c = isRow ? i : kingCol;

            if ((isRow && c == kingCol) || (!isRow && r == kingRow))
                continue; // Skip king position itself

            // Citadels and throne block king's movement
            if (BaselineHeuristicsUtils.isInCitadel(r, c))
                score += currentPlayer == State.Turn.WHITE ? -1 : +1;
            if (BaselineHeuristicsUtils.isInThrone(r, c))
                score += currentPlayer == State.Turn.WHITE ? -1 : +1;

            // Escape evaluation
            if (BaselineHeuristicsUtils.isInEscape(r, c)) {
                int distToEscape = isRow ? Math.abs(kingCol - c) : Math.abs(kingRow - r);

                // Check if path between king and escape is free of black pawns
                boolean lineClear = true;
                int start = Math.min(isRow ? kingCol : kingRow, i) + 1;
                int end = Math.max(isRow ? kingCol : kingRow, i);
                for (int j = start; j < end; j++) {
                    int rr = isRow ? kingRow : j;
                    int cc = isRow ? j : kingCol;
                    if (board[rr][cc] == State.Pawn.BLACK) {
                        lineClear = false;
                        break;
                    }
                }

                // Assign penalties based on threat level
                if (lineClear) {
                    score += currentPlayer == State.Turn.WHITE ? 10 : -10; // King can escape directly
                } else if (distToEscape <= 2) {
                    score += currentPlayer == State.Turn.WHITE ? 3 : -5; // King is close to escape
                } else if (distToEscape <= 4) {
                    score += currentPlayer == State.Turn.WHITE ? 2 : -2; // King is not so close to escape
                }
            }
        }

        // Check black pawns ONLY on king's row/col
        for (Integer[] pos : blackPositions) {
            if (pos[0] == kingRow)
                score += currentPlayer == State.Turn.WHITE ? -1 : 1;  // Black on same row
            if (pos[1] == kingCol)
                score += currentPlayer == State.Turn.WHITE ? -1 : 1;  // Black on same column
        }

        // Check white pawns ONLY on king's row/col
        for (Integer[] pos : whitePositions) {
            if (pos[0] == kingRow)
                score += currentPlayer == State.Turn.WHITE ? 0.5 : -1;  // White on same row
            if (pos[1] == kingCol)
                score += currentPlayer == State.Turn.WHITE ? 0.5 : -1;  // White on same column
        }

        return score;
    }
}
