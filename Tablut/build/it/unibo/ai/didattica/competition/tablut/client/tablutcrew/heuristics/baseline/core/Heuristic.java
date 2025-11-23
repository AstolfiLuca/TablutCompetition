package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.core;

import com.google.gson.Gson;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.utils.BaselineHeuristicsUtils;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public abstract class Heuristic {
    public BaselineHeuristicsUtils.BoardState boardState;
    public State.Turn currentPlayer;
    private final Gson gson = new Gson();

    public Heuristic(BaselineHeuristicsUtils.BoardState boardState, State.Turn currentPlayer) {
        this.boardState = boardState;
        this.currentPlayer = currentPlayer;
    }

    public String toJson(State state){
        return gson.toJson(state);
    }

    public double logAndEvaluate(State state){
        System.out.println("Sto valutando l'euristica " + this.getClass().getSimpleName() + " per il seguente stato:");
        System.out.println(gson.toJson(state));
        double value = this.evaluateState(state);
        System.out.println("Valutazione: " + value);
        return value;
    }
    public abstract double evaluateState(State state);

    /**
     * Min-Max normalization helper
     * @param value the raw value to normalize
     * @param min the minimum possible value
     * @param max the maximum possible value
     * @return normalized value in [0, 1]
     */
    protected double normalize(double value, double min, double max) {
        if (Math.abs(min - max) == 0) return 0; // Avoid division by zero
        double normalized = (value - min) / (max - min);
        return Math.max(0, Math.min(1.0, normalized)); // Clamp to [0, 1] if it falls outside the range
    }
}
