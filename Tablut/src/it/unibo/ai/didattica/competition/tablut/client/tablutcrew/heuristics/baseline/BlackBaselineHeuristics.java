package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.Heuristic;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.List;

public class BlackBaselineHeuristics implements Heuristic {
    public State state;
    private static final double KING_THREAT_WEIGHT = 5.0;
    private static final double MANHATTAN_SCORE_WEIGHT = 5.0;
    private static final double LINE_SCORE_WEIGHT = 5.0;
    private static final double PIECES_SCORE_WEIGHT = 50.0;

    public BlackBaselineHeuristics(State state) {
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
        double manhattanDistanceToKingScore = manhattanOverallScoreToKing(kingRow, kingCol, stats.getBlackPositions()) * MANHATTAN_SCORE_WEIGHT;
        double piecesScore = (blackPawns - (whitePawns + 1)) * PIECES_SCORE_WEIGHT;
        double lineScore = evaluateKingLinePosition(
                state.getBoard(),
                kingRow,
                kingCol,
                stats.getBlackPositions(),
                stats.getWhitePositions()
        ) * LINE_SCORE_WEIGHT;
        double kingThreatScore = kingAdjacentAttackers * KING_THREAT_WEIGHT;

        return piecesScore + kingThreatScore + manhattanDistanceToKingScore + lineScore;
    }

    /**
     * Calculates a heuristic score based on the total (Manhattan) distance
     * between all black pawns and the king.
     *
     * The idea is that the closer the black pawns are to the king, the smaller
     * the total Manhattan distance — so this function computes that total distance,
     * then converts it into a "score" by comparing it against a maximum possible distance.
     *
     * @param kingRow the row index of the king
     * @param kingCol the column index of the king
     * @param blackPositions the list of all black pawns’ positions, where each position
     *                       is an Integer[] {row, col}
     * @return a score representing how close black pawns are to the king — the higher
     *         the score, the closer (or more threatening) they are
     */
    public int manhattanOverallScoreToKing(int kingRow, int kingCol, List<Integer[]> blackPositions){
        int maximumOverallDistance = 211;
        int overallDistance = 0;
        Integer[] kingPosition = new Integer[]{kingRow, kingCol};
        for (Integer[] blackPawnPosition : blackPositions)
            overallDistance += BaselineHeuristicsUtils.manhattanDistance(kingPosition, blackPawnPosition);
        return Math.abs(overallDistance - maximumOverallDistance);
    }

    private double evaluateKingLinePosition(
            State.Pawn[][] board,
            int kingRow,
            int kingCol,
            List<Integer[]> blackPositions,
            List<Integer[]> whitePositions
    ) {
        double rowScore = BaselineHeuristicsUtils.evaluateKingDirection(board, kingRow, kingCol, blackPositions, whitePositions, State.Turn.BLACK, true);   // row
        double colScore = BaselineHeuristicsUtils.evaluateKingDirection(board, kingRow, kingCol, blackPositions, whitePositions, State.Turn.BLACK, false);  // column
        return rowScore + colScore;
    }
}
