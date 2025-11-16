package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class KingSafety extends Heuristic {

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

        return currentPlayer == State.Turn.WHITE ? adjacentDefenders : -adjacentDefenders;
    }
}
