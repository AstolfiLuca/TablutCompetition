package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.Heuristic;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class WhiteHeuristics implements Heuristic {
    public State state;
    private static final double KING_THREAT_BONUS = 5.0;

    public WhiteHeuristics(State state) {
        this.state = state;
    }

    @Override
    public double evaluateState() {
        HeuristicsUtils.BoardState stats = HeuristicsUtils.analyzeBoard(state);

        int whitePawns = stats.getWhitePawns() + 1;
        int blackPawns = stats.getBlackPawns();
        // King position on the board (always present because the state is not terminal)
        int kingRow = stats.getKingRow();
        int kingCol = stats.getKingCol();

        // Count how many black pawns are adjacent to the king.
        int kingAdjacentAttackers = HeuristicsUtils.getAdjacentAttackers(kingRow, kingCol, state.getBoard());


        double kingThreatScore = kingAdjacentAttackers * KING_THREAT_BONUS;
        // Score = white pieces - black pieces (+ bonus for king)
        double absoluteWhiteScore = (whitePawns + 1) - blackPawns - kingThreatScore;
        return absoluteWhiteScore;
    }
}
