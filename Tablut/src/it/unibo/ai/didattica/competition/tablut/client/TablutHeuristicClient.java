package it.unibo.ai.didattica.competition.tablut.client;

import aima.core.search.adversarial.AdversarialSearch;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public abstract class TablutHeuristicClient extends TablutClient {
    protected Map<String, Double> weights = new HashMap<>();
    public List<String> keys;
    protected AdversarialSearch<State, Action> searchStrategy;
    public final static int REPEATED_MOVES_ALLOWED = 0;
    public final static int CACHED_MOVES_ALLOWED = -1;
    public final static String LOGS_FOLDER = "logs";


    public TablutHeuristicClient(
            String player,
            String name,
            int timeout,
            String ipAddress,
            String weights,
            AdversarialSearch<State, Action> searchStrategy
    ) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress);
        this.searchStrategy = searchStrategy;
        parseOptionalWeights(weights);
    }

    public TablutHeuristicClient(String player, String name, int timeout, String ipAddress, String weights) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress);
        parseOptionalWeights(weights);
    }

    public TablutHeuristicClient(String player, String name, int timeout, String ipAddress, AdversarialSearch<State, Action> searchStrategy) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress);
        this.searchStrategy = searchStrategy;
    }

    public TablutHeuristicClient(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress);
    }

    public TablutHeuristicClient(String player, String name, int timeout) throws UnknownHostException, IOException {
        super(player, name, timeout);
    }

    public TablutHeuristicClient(String player, String name) throws UnknownHostException, IOException {
        super(player, name);
    }

    public TablutHeuristicClient(String player, String name, String ipAddress) throws UnknownHostException, IOException {
        super(player, name, ipAddress);
    }

    private void parseOptionalWeights(String weights) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.weights = mapper.readValue(weights, Map.class);
            System.out.println("Loaded heuristic weights: " + this.weights);
        } catch (Exception e) {
            System.out.println("Error parsing weights JSON: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            this.declareName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        State state = new StateTablut();
        List<int[]> pawns = new ArrayList<int[]>();
        List<int[]> empty = new ArrayList<int[]>();

        // Game loop
        while(true){
            try {
                // Try to read the state from the server (blocking call)
                this.read();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                System.exit(2);
            }
            // Update the state received
            state = this.getCurrentState();
            if (this.getPlayer().equals(State.Turn.WHITE)) {
                if (state.getTurn().equals(StateTablut.Turn.WHITE)) {
                    // Find best move
                    Action bestAction = searchStrategy.makeDecision(state);
                    try {
                        this.write(bestAction);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                }
                // Opponent round
                else if (state.getTurn().equals(StateTablut.Turn.BLACK)) {
                    System.out.println("Waiting for your opponent move... ");
                }
                // White player wins
                else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
                    System.out.println("YOU WIN!");
                    System.exit(0);
                }
                // White player loses
                else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
                    System.out.println("YOU LOSE!");
                    System.exit(0);
                }
                // Draw
                else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
                    System.out.println("DRAW!");
                    System.exit(0);
                }
            } else {
                if (state.getTurn().equals(StateTablut.Turn.BLACK)) {
                    // Find best move
                    Action bestAction = searchStrategy.makeDecision(state);
                    try {
                        this.write(bestAction);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                }
                // Opponent round
                else if (state.getTurn().equals(StateTablut.Turn.WHITE)) {
                    System.out.println("Waiting for your opponent move... ");
                }
                // Black player wins
                else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
                    System.out.println("YOU WIN!");
                    System.exit(0);
                }
                // Black player loses
                else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
                    System.out.println("YOU LOSE!");
                    System.exit(0);
                }
                // Draw
                else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
                    System.out.println("DRAW!");
                    System.exit(0);
                }
            }
        }
    }

}
