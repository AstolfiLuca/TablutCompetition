package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline;

import aima.core.search.adversarial.AdversarialSearch;
import it.unibo.ai.didattica.competition.tablut.client.TablutHeuristicClient;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.weights.BlackWeights;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.heuristics.baseline.weights.WhiteWeights;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.search.BaselineSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.io.IOException;
import java.net.UnknownHostException;

public class BaselinePlayer extends TablutHeuristicClient {
    public final static String CLIENT_NAME = "TablutCrew";
    public final static String TEAM_NAME = "TablutCrew Team";
    // If the same state of the game is reached twice, draw
    public final static int REPEATED_MOVES_ALLOWED = 0;
    public final static int CACHED_MOVES_ALLOWED = -1;
    public final static String LOGS_FOLDER = "logs";

    public BaselinePlayer(String player, String name, int timeout, String ipAddress, String weights, AdversarialSearch<State, Action> searchStrategy) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress, weights, searchStrategy);
    }

    public BaselinePlayer(String player, String name, int timeout, String ipAddress, String weights) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress, weights);
        GameAshtonTablut game = new GameAshtonTablut(REPEATED_MOVES_ALLOWED, CACHED_MOVES_ALLOWED, LOGS_FOLDER, "White", "Black");
        this.searchStrategy = new BaselineSearch(game, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, timeout, this.weights);
    }

    public BaselinePlayer(String player, String name, int timeout, String ipAddress, AdversarialSearch<State, Action> searchStrategy, Double ...weights) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress, searchStrategy);
        initializeWeights();
        updateWeights(weights);
        GameAshtonTablut game = new GameAshtonTablut(REPEATED_MOVES_ALLOWED, CACHED_MOVES_ALLOWED, LOGS_FOLDER, "White", "Black");
        this.searchStrategy = new BaselineSearch(game, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, timeout, this.weights);
    }

    public BaselinePlayer(String player, String name, int timeout, String ipAddress, Double ...weights) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress);
        initializeWeights();
        updateWeights(weights);
        GameAshtonTablut game = new GameAshtonTablut(REPEATED_MOVES_ALLOWED, CACHED_MOVES_ALLOWED, LOGS_FOLDER, "White", "Black");
        this.searchStrategy = new BaselineSearch(game, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, timeout, this.weights);
    }

    public BaselinePlayer(String player, String name, int timeout) throws UnknownHostException, IOException {
        super(player, name, timeout);
    }

    public BaselinePlayer(String player, String name) throws UnknownHostException, IOException {
        super(player, name);
    }

    public BaselinePlayer(String player, String name, String ipAddress) throws UnknownHostException, IOException {
        super(player, name, ipAddress);
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        String role = "";
        String name = TEAM_NAME + " " + CLIENT_NAME;
        String ipAddress = "localhost";
        String weights = null;
        int timeout = 60;

        if (args.length < 1) {
            System.out.println("You must specify which player you are (WHITE or BLACK)");
            System.exit(-1);
        }

        role = args[0];
        System.out.println("Role: " + role);

        if (args.length > 1) {
            timeout = Integer.parseInt(args[1]);
            System.out.println("Timeout: " + timeout);
        }
        if (args.length > 2) {
            ipAddress = args[2];
            System.out.println("IP Address: " + ipAddress);
        }
        if (args.length > 3) {
            weights = args[3];
            System.out.println("Weights: " + weights);
        }
        System.out.println("Selected client: " + args[0]);

        BaselinePlayer client = new BaselinePlayer(role, name, timeout, ipAddress, weights);
        client.run();
    }

    private void initializeWeights() {
        this.keys = this.getPlayer() == State.Turn.BLACK ? BlackWeights.getAllKeys() : WhiteWeights.getAllKeys();
        this.weights = this.getPlayer() == State.Turn.BLACK ? BlackWeights.getDefaultWeights() : WhiteWeights.getDefaultWeights();
    }

    private void updateWeights(Double[] weights) {
        int i = 0;
        // Store the passed weights
        while (i < weights.length && i < keys.size()) {
            this.weights.put(keys.get(i), weights[i]);
            i += 1;
        }
    }
}
