package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.HeuristicRegistry;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.weights.WhiteWeights;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.util.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * White player composite heuristic that combines multiple heuristics
 * based on provided weights configuration.
 */
public class WhiteBaselineHeuristics extends Heuristic {
    private final Map<String, Double> weights;
    private final State state;

    /**
     * Constructor with string-keyed weights map
     */
    public WhiteBaselineHeuristics(State state, State.Turn currentPlayer, Map<String, Double> weights) {
        super(BaselineHeuristicsUtils.analyzeBoard(state), currentPlayer);
        this.state = state;

        // Use only provided weights if given, otherwise use defaults
        if (weights == null || weights.isEmpty()) {
            this.weights = WhiteWeights.getDefaultWeights();
        } else {
            // Filter to only use weights that are registered
            this.weights = filterValidWeights(weights);
        }
    }

    /**
     * Constructor with default weights
     */
    public WhiteBaselineHeuristics(State state, State.Turn currentPlayer) {
        this(state, currentPlayer, WhiteWeights.getDefaultWeights());
    }

    /**
     * Filters provided weights to only include registered heuristics
     */
    private Map<String, Double> filterValidWeights(Map<String, Double> providedWeights) {
        Map<String, Double> filtered = new HashMap<>();

        for (Map.Entry<String, Double> entry : providedWeights.entrySet()) {
            String key = entry.getKey();

            // Only include if it's a known weight key
            if (WhiteWeights.getAllKeys().contains(key)) {
                filtered.put(key, entry.getValue());
            } else {
                System.err.println("Warning: Unknown weight key '" + key + "' - skipping");
            }
        }

        return filtered;
    }

    @Override
    public double evaluateState(State state) {
        double totalScore = 0.0;

        // boardState is already computed in constructor and stored in parent class
        BaselineHeuristicsUtils.BoardState boardState = this.boardState;

        // Loop through all provided weights
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            String key = entry.getKey();
            double weight = entry.getValue();

            // Skip if weight is zero (optimization)
            if (weight == 0.0) {
                continue;
            }

            // Check if this heuristic is registered in the registry
            if (HeuristicRegistry.hasWhiteHeuristic(key)) {
                try {
                    // Create heuristic instance with boardState
                    Heuristic heuristic = HeuristicRegistry.createWhiteHeuristic(key, boardState, currentPlayer);

                    // Evaluate the heuristic
                    double heuristicValue;
                    if (Configuration.debug){
                        heuristicValue =  heuristic.logAndEvaluate(state);
                    }else {
                        heuristicValue =  heuristic.evaluateState(state);
                    }

                    // Add weighted contribution to total score
                    totalScore += weight * heuristicValue;

                } catch (Exception e) {
                    System.err.println("Error evaluating heuristic '" + key + "': " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Warning: No heuristic implementation registered for key '" + key + "' - skipping");
            }
        }
        if (Configuration.debug){
            System.out.println("Total Score for this state");
            System.out.println(this.toJson(state));
            System.out.println(totalScore);
        }
        return totalScore;
    }

    /**
     * Get the weights being used by this heuristic
     */
    public Map<String, Double> getWeights() {
        return new HashMap<>(weights);
    }

    /**
     * Get the number of active heuristics (non-zero weights)
     */
    public int getActiveHeuristicCount() {
        return (int) weights.values().stream().filter(w -> w != 0.0).count();
    }
}