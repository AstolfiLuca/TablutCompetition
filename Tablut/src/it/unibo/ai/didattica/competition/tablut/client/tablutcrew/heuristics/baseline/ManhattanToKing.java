package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.List;

public class ManhattanToKing extends Heuristic {

    public ManhattanToKing(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        super(boardState, currentPlayer);
    }

    /**
     * Calculates a heuristic score based on the total (Manhattan) distance
     * between all black pawns and the king.
     *
     * The idea is that the closer the black pawns are to the king, the smaller
     * the total Manhattan distance — so this function computes that total distance,
     * then converts it into a "score" by comparing it against a maximum possible distance.
     *
     * @return a score representing how close black pawns are to the king — the higher
     *         the score, the closer (or more threatening) they are
     */
    @Override
    public double evaluateState(State state) {
        int kingRow = boardState.getKingRow();
        int kingCol = boardState.getKingCol();
        List<Integer[]> blackPositions = boardState.getBlackPositions();

        int maximumOverallDistance = 211;
        int overallDistance = 0;
        Integer[] kingPosition = new Integer[]{kingRow, kingCol};
        for (Integer[] blackPawnPosition : blackPositions)
            overallDistance += BaselineHeuristicsUtils.manhattanDistance(kingPosition, blackPawnPosition);
        int score = Math.abs(maximumOverallDistance - overallDistance);
        return currentPlayer == State.Turn.BLACK ? score : -score;
    }
}
