package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.search;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class TablutCrewSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {

    public TablutCrewSearch(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    @Override
    protected double eval(State state, State.Turn player) {
        // Needed to make heuristicEvaluationUsed = true, if the state evaluated isn't terminal
        super.eval(state, player);

        // Return heuristic value for the given state
        return game.getUtility(state, player);
    }


    private double simpleHeuristicEvaluation(State state, State.Turn player) {
        // Count pieces on the board
        int whitePawns = 0;
        int blackPawns = 0;
        boolean kingAlive = false;

        State.Pawn[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                State.Pawn pawn = board[i][j];
                if (pawn == State.Pawn.WHITE) {
                    whitePawns++;
                } else if (pawn == State.Pawn.BLACK) {
                    blackPawns++;
                } else if (pawn == State.Pawn.KING) {
                    kingAlive = true;
                }
            }
        }

        // Calculate score based on player perspective
        if (player == State.Turn.WHITE) {
            // White wants to keep the king alive and maximize white pieces
            if (!kingAlive) {
                return utilMin; // King is dead, worst possible outcome
            }
            // Score = white pieces - black pieces (+ bonus for king)
            return (whitePawns + 1) - blackPawns; // +1 for the king
        } else {
            // Black wants to kill the king and maximize black pieces
            if (!kingAlive) {
                return utilMax; // King is dead, best possible outcome for black
            }
            // Score = black pieces - white pieces (- penalty for king being alive)
            return blackPawns - (whitePawns + 1); // +1 for the king
        }
    }
}
