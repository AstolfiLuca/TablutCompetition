package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.*;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.weights.BlackWeights;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.weights.WhiteWeights;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Registry that maps heuristic weight keys to their corresponding Heuristic classes.
 */
public class HeuristicRegistry {

    // Function<BoardState, Heuristic> - only takes BoardState
    private static final Map<String, BiFunction<BaselineHeuristicsUtils.BoardState, State.Turn, Heuristic>> BLACK_HEURISTICS = new HashMap<>();
    private static final Map<String, BiFunction<BaselineHeuristicsUtils.BoardState, State.Turn, Heuristic>> WHITE_HEURISTICS = new HashMap<>();

    static {
        initializeBlackHeuristics();
        initializeWhiteHeuristics();
    }

    /**
     * Initialize all Black player heuristics
     */
    private static void initializeBlackHeuristics() {
        // King-related heuristics
        registerBlack(BlackWeights.KING_THREAT_WEIGHT, KingThreat::new);
        registerBlack(BlackWeights.KING_SAFETY_WEIGHT, KingSafety::new);
        registerBlack(BlackWeights.KING_INCHECK_WEIGHT, KingInCheck::new);

        // Distance heuristics
        registerBlack(BlackWeights.MANHATTAN_TOKING_WEIGHT, ManhattanToKing::new);
        registerBlack(BlackWeights.MANHATTAN_TOESCAPES_WEIGHT, ManhattanToEscapes::new);

        // Board control heuristics
        registerBlack(BlackWeights.LINE_SCORE_WEIGHT, KingLineEvaluation::new);

        // Pawn-related heuristics
        registerBlack(BlackWeights.PAWNS_SAFETY_WEIGHT, PawnsSafety::new);
        registerBlack(BlackWeights.PAWNS_THREAT_SCORE_WEIGHT, PawnsThreat::new);

        // Material and positioning
        registerBlack(BlackWeights.PIECES_SCORE_WEIGHT, PieceCount::new);
        registerBlack(BlackWeights.DEFENSIVE_POSITION_WEIGHT, DefensivePosition::new);
        registerBlack(BlackWeights.RHOMBUS_SCORE_WEIGHT, RhombusPosition::new);
    }

    /**
     * Initialize all White player heuristics
     */
    private static void initializeWhiteHeuristics() {
        // King-related heuristics
        registerWhite(WhiteWeights.KING_THREAT_WEIGHT, KingThreat::new);
        registerWhite(WhiteWeights.KING_SAFETY_WEIGHT, KingSafety::new);
        registerWhite(WhiteWeights.KING_INCHECK_WEIGHT, KingInCheck::new);

        // Distance heuristics
        registerWhite(WhiteWeights.MANHATTAN_TOKING_WEIGHT, ManhattanToKing::new);
        registerWhite(WhiteWeights.MANHATTAN_TOESCAPES_WEIGHT, ManhattanToEscapes::new);

        // Board control heuristics
        registerWhite(WhiteWeights.LINE_SCORE_WEIGHT, KingLineEvaluation::new);

        // Pawn-related heuristics
        registerWhite(WhiteWeights.PAWNS_SAFETY_WEIGHT, PawnsSafety::new);
        registerWhite(WhiteWeights.PAWNS_THREAT_SCORE_WEIGHT, PawnsThreat::new);

        // Material and positioning
        registerWhite(WhiteWeights.PIECES_SCORE_WEIGHT, PieceCount::new);
        registerWhite(WhiteWeights.DEFENSIVE_POSITION_WEIGHT, DefensivePosition::new);
        registerWhite(WhiteWeights.RHOMBUS_SCORE_WEIGHT, RhombusPosition::new);
    }

    /**
     * Register a black heuristic
     */
    public static void registerBlack(String key, BiFunction<BaselineHeuristicsUtils.BoardState, State.Turn, Heuristic> constructor) {
        if (BLACK_HEURISTICS.containsKey(key)) {
            System.err.println("Warning: Overwriting existing black heuristic for key: " + key);
        }
        BLACK_HEURISTICS.put(key, constructor);
    }

    /**
     * Register a white heuristic
     */
    public static void registerWhite(String key, BiFunction<BaselineHeuristicsUtils.BoardState, State.Turn, Heuristic> constructor) {
        if (WHITE_HEURISTICS.containsKey(key)) {
            System.err.println("Warning: Overwriting existing white heuristic for key: " + key);
        }
        WHITE_HEURISTICS.put(key, constructor);
    }

    /**
     * Creates a Black heuristic instance from a weight key.
     * Only BoardState is passed to constructor.
     */
    public static Heuristic createBlackHeuristic(String key, BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        BiFunction<BaselineHeuristicsUtils.BoardState, State.Turn, Heuristic> constructor = BLACK_HEURISTICS.get(key);
        if (constructor == null) {
            throw new IllegalArgumentException("No black heuristic registered for key: " + key);
        }
        return constructor.apply(boardState, currentPlayer);
    }

    /**
     * Creates a White heuristic instance from a weight key.
     * Only BoardState is passed to constructor.
     */
    public static Heuristic createWhiteHeuristic(String key, BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        BiFunction<BaselineHeuristicsUtils.BoardState, State.Turn, Heuristic> constructor = WHITE_HEURISTICS.get(key);
        if (constructor == null) {
            throw new IllegalArgumentException("No white heuristic registered for key: " + key);
        }
        return constructor.apply(boardState, currentPlayer);
    }

    /**
     * Checks if a black heuristic is registered
     */
    public static boolean hasBlackHeuristic(String key) {
        return BLACK_HEURISTICS.containsKey(key);
    }

    /**
     * Checks if a white heuristic is registered
     */
    public static boolean hasWhiteHeuristic(String key) {
        return WHITE_HEURISTICS.containsKey(key);
    }

    /**
     * Returns all registered black heuristic keys
     */
    public static java.util.Set<String> getBlackHeuristicKeys() {
        return new java.util.HashSet<>(BLACK_HEURISTICS.keySet());
    }

    /**
     * Returns all registered white heuristic keys
     */
    public static java.util.Set<String> getWhiteHeuristicKeys() {
        return new java.util.HashSet<>(WHITE_HEURISTICS.keySet());
    }
}