package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;

public class DefensivePosition extends Heuristic {
    // Theoretical bounds
    private static final Map<State.Turn, Double> MIN_VALUES = new HashMap<>();
    private static final Map<State.Turn, Double> MAX_VALUES = new HashMap<>();

    static {
        // Black perspective
        MIN_VALUES.put(State.Turn.BLACK, 0.0);
        MAX_VALUES.put(State.Turn.BLACK, 16.0);

        // White perspective
        MIN_VALUES.put(State.Turn.WHITE, -16.0);
        MAX_VALUES.put(State.Turn.WHITE, 0.0);
    }

    public DefensivePosition(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    /**
     * Rewards blacks positioned to control key board areas.
     * Different from rhombus - focuses on defensive formations.
     * */
    @Override
    public double evaluateState(State state) {
        int score = 0;
        State.Pawn[][] board = state.getBoard();

        // Key defensive rings around center
        int[][] defensiveRing1 = {
                {2,2}, {2,3}, {2,4},
                {3,2},        {3,4},
                {4,2}, {4,3}, {4,4}
        };

        int[][] defensiveRing2 = {
                {1,1}, {1,2}, {1,3}, {1,4}, {1,5},
                {2,1},                      {2,5},
                {3,1},                      {3,5},
                {4,1},                      {4,5},
                {5,1}, {5,2}, {5,3}, {5,4}, {5,5}
        };

        // Count blacks in inner ring (more valuable)
        for (int[] pos : defensiveRing1) {
            if (board[pos[0]][pos[1]] == State.Pawn.BLACK) {
                score += 2;
            }
        }
        // Count blacks in outer ring
        for (int[] pos : defensiveRing2) {
            if (board[pos[0]][pos[1]] == State.Pawn.BLACK) {
                score += 1;
            }
        }
        double minValue = MIN_VALUES.get(currentPlayer);
        double maxValue = MAX_VALUES.get(currentPlayer);

        score = currentPlayer == State.Turn.BLACK ? score : -score;
        return normalize(score, minValue, maxValue);
    }
}
