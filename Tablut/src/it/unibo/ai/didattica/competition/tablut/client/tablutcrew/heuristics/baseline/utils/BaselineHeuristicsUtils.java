package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils;

import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.ArrayList;
import java.util.List;

public class BaselineHeuristicsUtils {
    public BaselineHeuristicsUtils() {}

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

    /**
     * Counts how many enemy pawns are currently in danger of being captured
     * by the given player, according to the static board position.
     *
     * @param state the current game state
     * @param player the player whose capturing potential we are evaluating
     * @return the number of enemy pawns that are currently capturable by the current player
     */
    public static int countCapturablePawns(State state, State.Turn player) {
        State.Pawn[][] board = state.getBoard();
        int n = board.length;
        int count = 0;

        State.Pawn ally = player == State.Turn.BLACK ? State.Pawn.BLACK : State.Pawn.WHITE;
        State.Pawn enemy = ally == State.Pawn.WHITE ? State.Pawn.BLACK : State.Pawn.WHITE;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == ally) continue;

                if (ally == State.Pawn.BLACK && board[r][c] == State.Pawn.KING) {
                    if (isCapturableKing(board, r, c))
                        count++;
                } else if (board[r][c] == enemy) {
                    // Check four directions
                    if (isCapturable(board, r, c, -1, 0, ally) || // up
                            isCapturable(board, r, c, 1, 0, ally) || // down
                            isCapturable(board, r, c, 0, -1, ally) || // left
                            isCapturable(board, r, c, 0, 1, ally))  // right
                        count++;
                }
            }
        }

        return count;
    }

    public static boolean isCapturableKing(State.Pawn[][] board, int kingRow, int kingCol){
        int n = board.length;
        State.Pawn ally = State.Pawn.BLACK;
        // Check if the position is valid
        if (!inBounds(kingRow, kingCol, n)) return false;
        if (isInThrone(kingRow, kingCol)){
            int surroundedByBlack = 0;
            // Check the four directions — King captured if surrounded by 3 BLACK pawns
            if (inBounds(kingRow - 1, kingCol, n) && board[kingRow - 1][kingCol] == State.Pawn.BLACK) surroundedByBlack++;
            if (inBounds(kingRow + 1, kingCol, n) && board[kingRow + 1][kingCol] == State.Pawn.BLACK) surroundedByBlack++;
            if (inBounds(kingRow, kingCol - 1, n) && board[kingRow][kingCol - 1] == State.Pawn.BLACK) surroundedByBlack++;
            if (inBounds(kingRow, kingCol + 1, n) && board[kingRow][kingCol + 1] == State.Pawn.BLACK) surroundedByBlack++;
            return surroundedByBlack >= 3;
        } else if (isAdjacentToThrone(kingRow, kingCol)){
            int surroundedByBlack = 0;
            // For each direction, check if throne or BLACK pawn are adjacent
            if (inBounds(kingRow - 1, kingCol, n) &&
                    (isInThrone(kingRow - 1, kingCol) || board[kingRow - 1][kingCol] == State.Pawn.BLACK))
                surroundedByBlack++;
            if (inBounds(kingRow + 1, kingCol, n) &&
                    (isInThrone(kingRow + 1, kingCol) || board[kingRow + 1][kingCol] == State.Pawn.BLACK))
                surroundedByBlack++;
            if (inBounds(kingRow, kingCol - 1, n) &&
                    (isInThrone(kingRow, kingCol - 1) || board[kingRow][kingCol - 1] == State.Pawn.BLACK))
                surroundedByBlack++;
            if (inBounds(kingRow, kingCol + 1, n) &&
                    (isInThrone(kingRow, kingCol + 1) || board[kingRow][kingCol + 1] == State.Pawn.BLACK))
                surroundedByBlack++;

            // King is captured if surrounded by 2 or more blocking sides (BLACK or throne)
            return surroundedByBlack >= 2;
        } else {
            return isCapturable(board, kingRow, kingCol, -1, 0, ally) || // Up
                    isCapturable(board, kingRow, kingCol, 1, 0, ally)  || // Down
                    isCapturable(board, kingRow, kingCol, 0, -1, ally) || // Left
                    isCapturable(board, kingRow, kingCol, 0, 1, ally);   // Right
        }
    }

    /**
     * Checks if a pawn at position (r, c) is capturable *in the given direction* (dr, dc)
     * based on the Tablut "sandwich" capture rule.
     *
     * @param board the current game board
     * @param r the row of the pawn being checked
     * @param c the column of the pawn being checked
     * @param dr the row direction (±1 or 0)
     * @param dc the column direction (±1 or 0)
     * @param ally the pawn type of the capturing side (e.g., BLACK if checking captures by Black)
     * @return true if the pawn at (r, c) could be captured along this direction
     */
    public static boolean isCapturable(State.Pawn[][] board, int r, int c, int dr, int dc,
                                 State.Pawn ally) {
        int n = board.length;
        int r1 = r + dr, c1 = c + dc;   // square on one side
        int r2 = r - dr, c2 = c - dc;   // square on opposite side

        if (!inBounds(r1, c1, n) || !inBounds(r2, c2, n)) return false;

        State.Pawn p1 = board[r1][c1];
        State.Pawn p2 = board[r2][c2];

        // The "capturing sandwich" condition:
        boolean supportSide = (p2 == ally || p2 == State.Pawn.THRONE || isInCitadel(r2, c2));
        boolean freeSide = (p1 == State.Pawn.EMPTY);

        return supportSide && freeSide;
    }

    public static boolean inBounds(int r, int c, int n) {
        return r >= 0 && c >= 0 && r < n && c < n;
    }

    /**
     * Checks whether a given square (r, c) is adjacent (up, down, left, or right) to throne.
     */
    public static boolean isAdjacentToThrone(int r, int c) {
        int throneRow = 4, throneCol = 4;

        // A square is adjacent if it's exactly one move away horizontally or vertically
        return (r == throneRow && Math.abs(c - throneCol) == 1) ||
                (c == throneCol && Math.abs(r - throneRow) == 1);
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
