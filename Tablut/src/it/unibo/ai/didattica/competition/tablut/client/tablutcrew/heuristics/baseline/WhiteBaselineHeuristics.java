package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.Heuristic;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.List;

public class WhiteBaselineHeuristics implements Heuristic {
    public State state;
    private static final double KING_THREAT_WEIGHT = 5.0;
    private static final double MANHATTAN_SCORE_WEIGHT = 5.0;
    private static final double LINE_SCORE_WEIGHT = 5.0;
    private static final double PIECES_SCORE_WEIGHT = 50.0;

    public WhiteBaselineHeuristics(State state) {
        this.state = state;
    }

    @Override
    public double evaluateState() {
        BaselineHeuristicsUtils.BoardState stats = BaselineHeuristicsUtils.analyzeBoard(state);

        int whitePawns = stats.getWhitePawns() + 1;
        int blackPawns = stats.getBlackPawns();
        // King position on the board (always present because the state is not terminal)
        int kingRow = stats.getKingRow();
        int kingCol = stats.getKingCol();

        // Count how many black pawns are adjacent to the king.
        int kingAdjacentAttackers = BaselineHeuristicsUtils.getAdjacentAttackers(kingRow, kingCol, state.getBoard());

        // Compute heuristics
        double piecesScore = ((whitePawns + 1) - blackPawns) * PIECES_SCORE_WEIGHT;
        double manhattanDistanceToEscapeScore = manhattanToClosestEscape(kingRow, kingCol) * MANHATTAN_SCORE_WEIGHT;
        double kingThreatScore = kingAdjacentAttackers * KING_THREAT_WEIGHT;
        double lineScore = evaluateKingLinePosition(
                state.getBoard(),
                kingRow,
                kingCol,
                stats.getBlackPositions(),
                stats.getWhitePositions()
        ) * LINE_SCORE_WEIGHT;

        return piecesScore - kingThreatScore + manhattanDistanceToEscapeScore + lineScore;
    }

    /**
     * Approximated Manhattan distance to the closest *reachable* escape
     * (does not count obstacles in the way)
     */
    private int manhattanToClosestEscape(int kingRow, int kingCol) {
        int[][] escapes = {
                {0, 1}, {0, 2}, {0, 6}, {0, 7},
                {1, 0}, {1, 8}, {2, 0}, {2, 8},
                {6, 0}, {6, 8}, {7, 0}, {7, 8},
                {8, 1}, {8, 2}, {8, 6}, {8, 7}
        };
        int minDistance = Integer.MAX_VALUE;

        for (int[] escape : escapes) {
            int r = escape[0], c = escape[1];
            int dist = Math.abs(kingRow - r) + Math.abs(kingCol - c);

            minDistance = Math.min(minDistance, dist);
        }

        int maxPossibleDistance = 16;
        return Math.abs(maxPossibleDistance - minDistance);
    }

    private double evaluateKingLinePosition(
            State.Pawn[][] board,
            int kingRow,
            int kingCol,
            List<Integer[]> blackPositions,
            List<Integer[]> whitePositions
    ) {
        double rowScore = BaselineHeuristicsUtils.evaluateKingDirection(board, kingRow, kingCol, blackPositions, whitePositions, State.Turn.WHITE, true);   // row
        double colScore = BaselineHeuristicsUtils.evaluateKingDirection(board, kingRow, kingCol, blackPositions, whitePositions, State.Turn.WHITE, false);  // column
        return rowScore + colScore;
    }
}
