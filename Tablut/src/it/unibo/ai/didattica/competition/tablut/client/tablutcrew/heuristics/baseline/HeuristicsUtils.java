package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class HeuristicsUtils {

    public HeuristicsUtils() {}

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

        State.Pawn[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                State.Pawn pawn = board[i][j];
                if (pawn == State.Pawn.WHITE) {
                    whitePawns++;
                } else if (pawn == State.Pawn.BLACK) {
                    blackPawns++;
                } else if (pawn == State.Pawn.KING) {
                    kingRow = i;
                    kingCol = j;
                }
            }
        }

        return new BoardState(whitePawns, blackPawns, kingRow, kingCol);
    }

    /**
     * A simple container class that holds information about the current state of the board.
     * */
    public static class BoardState {
        private int whitePawns;
        private int blackPawns;
        private int kingRow;
        private int kingCol;

        public BoardState(int whitePawns, int blackPawns, int kingRow, int kingCol) {
            this.whitePawns = whitePawns;
            this.blackPawns = blackPawns;
            this.kingRow = kingRow;
            this.kingCol = kingCol;
        }

        public int getWhitePawns() { return whitePawns; }
        public int getBlackPawns() { return blackPawns; }
        public int getKingRow() { return kingRow; }
        public int getKingCol() { return kingCol; }
    }
}
