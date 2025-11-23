package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.weights;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the weight keys for White player heuristics.
 * This class ensures type safety and prevents typos when accessing weights.
 */
public class WhiteWeights {
    // Weight key constants
    public static final String KING_THREAT_WEIGHT = "KING_THREAT_WEIGHT";
    public static final String KING_SAFETY_WEIGHT = "KING_SAFETY_WEIGHT";
    public static final String KING_INCHECK_WEIGHT = "KING_INCHECK_WEIGHT";
    public static final String MANHATTAN_TOKING_WEIGHT = "MANHATTAN_TOKING_WEIGHT";
    public static final String MANHATTAN_TOESCAPES_WEIGHT = "MANHATTAN_TOESCAPES_WEIGHT";
    public static final String LINE_SCORE_WEIGHT = "LINE_SCORE_WEIGHT";
    public static final String PAWNS_SAFETY_WEIGHT = "PAWNS_SAFETY_WEIGHT";
    public static final String PAWNS_THREAT_SCORE_WEIGHT = "PAWNS_THREAT_SCORE_WEIGHT";
    public static final String PIECES_SCORE_WEIGHT = "PIECES_SCORE_WEIGHT";
    public static final String DEFENSIVE_POSITION_WEIGHT = "DEFENSIVE_POSITION_WEIGHT";
    public static final String RHOMBUS_SCORE_WEIGHT = "RHOMBUS_SCORE_WEIGHT";

    // Default values
    public static final double DEFAULT_KING_THREAT_WEIGHT = 0.0;
    public static final double DEFAULT_KING_SAFETY_WEIGHT = 3.0;
    public static final double DEFAULT_KING_INCHECK_WEIGHT = 5.0;
    public static final double DEFAULT_MANHATTAN_TOKING_WEIGHT = 0.0;
    public static final double DEFAULT_MANHATTAN_TOESCAPES_WEIGHT = 13.0;
    public static final double DEFAULT_LINE_SCORE_WEIGHT = 11.0;
    public static final double DEFAULT_PAWNS_SAFETY_WEIGHT = 4.0;
    public static final double DEFAULT_PAWNS_THREAT_SCORE_WEIGHT = 14.0;
    public static final double DEFAULT_DEFENSIVE_POSITION_WEIGHT = 0.0;
    public static final double DEFAULT_RHOMBUS_SCORE_WEIGHT = 0.0;
    public static final double DEFAULT_PIECES_SCORE_WEIGHT = 89.0;

    // Private constructor to prevent instantiation
    private WhiteWeights() {
        throw new AssertionError("Cannot instantiate WhiteWeights");
    }

    /**
     * Returns a map with default weights for Black player
     */
    public static Map<String, Double> getDefaultWeights() {
        Map<String, Double> defaults = new HashMap<>();
        defaults.put(KING_THREAT_WEIGHT, DEFAULT_KING_THREAT_WEIGHT);
        defaults.put(KING_SAFETY_WEIGHT, DEFAULT_KING_SAFETY_WEIGHT);
        defaults.put(KING_INCHECK_WEIGHT, DEFAULT_KING_INCHECK_WEIGHT);
        defaults.put(MANHATTAN_TOKING_WEIGHT, DEFAULT_MANHATTAN_TOKING_WEIGHT);
        defaults.put(MANHATTAN_TOESCAPES_WEIGHT, DEFAULT_MANHATTAN_TOESCAPES_WEIGHT);
        defaults.put(LINE_SCORE_WEIGHT, DEFAULT_LINE_SCORE_WEIGHT);
        defaults.put(PAWNS_SAFETY_WEIGHT, DEFAULT_PAWNS_SAFETY_WEIGHT);
        defaults.put(PAWNS_THREAT_SCORE_WEIGHT, DEFAULT_PAWNS_THREAT_SCORE_WEIGHT);
        defaults.put(DEFENSIVE_POSITION_WEIGHT, DEFAULT_DEFENSIVE_POSITION_WEIGHT);
        defaults.put(RHOMBUS_SCORE_WEIGHT, DEFAULT_RHOMBUS_SCORE_WEIGHT);
        defaults.put(PIECES_SCORE_WEIGHT, DEFAULT_PIECES_SCORE_WEIGHT);
        return defaults;
    }

    /**
     * Validates that all required weight keys are present in the map
     */
    public static boolean isValid(Map<String, Double> weights) {
        return weights.containsKey(KING_THREAT_WEIGHT) &&
                weights.containsKey(KING_SAFETY_WEIGHT) &&
                weights.containsKey(KING_INCHECK_WEIGHT) &&
                weights.containsKey(MANHATTAN_TOKING_WEIGHT) &&
                weights.containsKey(MANHATTAN_TOESCAPES_WEIGHT) &&
                weights.containsKey(LINE_SCORE_WEIGHT) &&
                weights.containsKey(PAWNS_SAFETY_WEIGHT) &&
                weights.containsKey(PAWNS_THREAT_SCORE_WEIGHT) &&
                weights.containsKey(DEFENSIVE_POSITION_WEIGHT) &&
                weights.containsKey(RHOMBUS_SCORE_WEIGHT) &&
                weights.containsKey(PIECES_SCORE_WEIGHT);
    }

    /**
     * Returns all weight keys as a list
     */
    public static List<String> getAllKeys() {
        return Arrays.asList(
                KING_THREAT_WEIGHT,
                KING_SAFETY_WEIGHT,
                KING_INCHECK_WEIGHT,
                MANHATTAN_TOKING_WEIGHT,
                MANHATTAN_TOESCAPES_WEIGHT,
                LINE_SCORE_WEIGHT,
                PAWNS_SAFETY_WEIGHT,
                PAWNS_THREAT_SCORE_WEIGHT,
                DEFENSIVE_POSITION_WEIGHT,
                RHOMBUS_SCORE_WEIGHT,
                PIECES_SCORE_WEIGHT
        );
    }

    /**
     * Merges provided weights with default weights.
     * Provided weights take precedence, missing keys are filled with defaults.
     */
    public static Map<String, Double> mergeWithDefaults(Map<String, Double> providedWeights) {
        Map<String, Double> merged = WhiteWeights.getDefaultWeights();
        merged.putAll(providedWeights);
        return merged;
    }
}
