package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class ManhattanToEscapes extends Heuristic {

    public ManhattanToEscapes(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    /**
     * Approximated Manhattan distance to the closest *reachable* escape
     * (does not count obstacles in the way)
     */
    @Override
    public double evaluateState(State state) {
        int kingRow = boardState.getKingRow();
        int kingCol = boardState.getKingCol();

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
        int score = Math.abs(maxPossibleDistance - minDistance);
        return currentPlayer == State.Turn.WHITE ? score : -score;
    }
}
