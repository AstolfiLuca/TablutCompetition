package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.ArrayList;
import java.util.List;

public class BaselineHeuristicsUtils {

    public BaselineHeuristicsUtils() {}

    /** Get the number of adjacent attackers of a given pawn (expressed in terms of
     *  its position (row, col)).
     *
     * @param row Row index of the piece we want to know the adjacent attackers
     * @param col Col index of the piece we want to know the adjacent attackers
     * @param board Board to scan
     * @return
     */
    public static int getAdjacentAttackers(int row, int col, State.Pawn[][] board){
        int adjacentAttackers = 0;
        int[] dr = {-1, 1, 0, 0}; // N, S, E, W
        int[] dc = {0, 0, 1, -1};

        State.Turn player = board[row][col].belongsTo(State.Turn.WHITE) ? State.Turn.WHITE : State.Turn.BLACK;
        State.Turn opponent = player.equals(State.Turn.WHITE) ? State.Turn.BLACK : State.Turn.WHITE;

        for (int i = 0; i < 4; i++) {
            int nr = row + dr[i];
            int nc = col + dc[i];
            // Check if the adjacent square is on the board
            if (nr >= 0 && nr < board.length && nc >= 0 && nc < board.length)
                if (board[nr][nc].belongsTo(opponent))
                    adjacentAttackers++;
        }

        return adjacentAttackers;
    }

    /**
     * This method iterates through the board matrix and determines:
     * <ul>
     *   <li>The number of white pawns</li>
     *   <li>The number of black pawns</li>
     *   <li>The king’s position (row and column)</li>
     *   <li>Whether the king is still alive</li>
     * </ul>
     *
     * @param state the current {@link State} containing the board configuration
     * @return a {@link BoardState} instance with piece counts and king status
     */
    public static BoardState analyzeBoard(State state) {
        int whitePawns = 0;
        int blackPawns = 0;
        int kingRow = -1;
        int kingCol = -1;
        List<Integer[]> whitePositions = new ArrayList<>();
        List<Integer[]> blackPositions = new ArrayList<>();

        State.Pawn[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                State.Pawn pawn = board[i][j];
                if (pawn == State.Pawn.WHITE) {
                    whitePositions.add(new Integer[]{i, j});
                    whitePawns++;
                } else if (pawn == State.Pawn.BLACK) {
                    blackPositions.add(new Integer[]{i, j});
                    blackPawns++;
                } else if (pawn == State.Pawn.KING) {
                    kingRow = i;
                    kingCol = j;
                }
            }
        }

        return new BoardState(whitePawns, blackPawns, kingRow, kingCol, whitePositions, blackPositions);
    }

    /**
     * Evaluates threats and bonuses along one direction (row or column)
     * */
    public static double evaluateKingDirection(
            State.Pawn[][] board,
            int kingRow,
            int kingCol,
            List<Integer[]> blackPositions,
            List<Integer[]> whitePositions,
            State.Turn currentPlayer,
            boolean isRow
    ) {
        double score = 0;
        for (int i = 0; i < 9; i++) {
            int r = isRow ? kingRow : i;
            int c = isRow ? i : kingCol;

            if ((isRow && c == kingCol) || (!isRow && r == kingRow))
                continue; // Skip king position itself

            // Citadels and throne block king's movement — good for black
            if (BaselineHeuristicsUtils.isInCitadel(r, c)) score++;
            if (BaselineHeuristicsUtils.isInThrone(r, c)) score++;

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

    /**
     * Get Manhattan distance between two points of a finite space
     *
     * @param x First point
     * @param y Second point
     * @return
     */
    public static int manhattanDistance(Integer[] x, Integer[] y){
        // In our case the first element of the coordinates
        // are the rows while the second represent the columns
        return Math.abs(x[0] - y[0]) + Math.abs(x[1] - y[1]);
    }

    // Fast position checks (inlined for performance)
    public static boolean isInCitadel(int row, int col) {
        return ((row == 0 || row == 8) && (col >= 3 && col <= 5)) ||
                ((col == 0 || col == 8) && (row >= 3 && row <= 5));
    }

    public static boolean isInThrone(int row, int col) {
        return row == 4 && col == 4;
    }

    public static boolean isInEscape(int row, int col) {
        return (row == 0 || row == 8) && (col == 0 || col == 8);
    }

    /**
     * A simple container class that holds information about the current state of the board.
     * */
    public static class BoardState {
        private int whitePawns;
        private int blackPawns;
        private int kingRow;
        private int kingCol;
        private List<Integer[]> whitePositions;
        private List<Integer[]> blackPositions;

        public BoardState(
                int whitePawns,
                int blackPawns,
                int kingRow,
                int kingCol,
                List<Integer[]> whitePositions,
                List<Integer[]> blackPositions
        ) {
            this.whitePawns = whitePawns;
            this.blackPawns = blackPawns;
            this.kingRow = kingRow;
            this.kingCol = kingCol;
            this.whitePositions = whitePositions;
            this.blackPositions = blackPositions;
        }

        public int getWhitePawns() { return whitePawns; }
        public int getBlackPawns() { return blackPawns; }
        public int getKingRow() { return kingRow; }
        public int getKingCol() { return kingCol; }
        public List<Integer[]> getWhitePositions() { return whitePositions; }
        public List<Integer[]> getBlackPositions() { return blackPositions; }
    }
}
