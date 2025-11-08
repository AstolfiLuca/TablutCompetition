package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline;

import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.search.TablutCrewSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class BaselinePlayer extends TablutClient {
    public final static String CLIENT_NAME = "TablutCrew";
    public final static String TEAM_NAME = "TablutCrew Team";
    // If the same state of the game is reached twice, draw
    public final static int REPEATED_MOVES_ALLOWED = 0;
    public final static int CACHED_MOVES_ALLOWED = -1;
    public final static String LOGS_FOLDER = "logs";



    public BaselinePlayer(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress);
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
        int timeout = 60;

        if (args.length < 1) {
            System.out.println("You must specify which player you are (WHITE or BLACK)");
            System.exit(-1);
        } else {
            System.out.println(args[0]);
            role = (args[0]);
        }
        if (args.length == 2) {
            System.out.println(args[1]);
            timeout = Integer.parseInt(args[1]);
        }
        if (args.length == 3) {
            ipAddress = args[2];
        }
        System.out.println("Selected client: " + args[0]);

        BaselinePlayer client = new BaselinePlayer(role, name, timeout, ipAddress);
        client.run();
    }

    @Override
    public void run() {
        try {
            this.declareName();
        } catch (Exception e) {
            e.printStackTrace();
        }


        State state = new StateTablut();
        GameAshtonTablut game = new GameAshtonTablut(
                REPEATED_MOVES_ALLOWED,
                CACHED_MOVES_ALLOWED,
                LOGS_FOLDER,
                "white",
                "black"
                );
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
                    TablutCrewSearch search = new TablutCrewSearch(game, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, this.getTimeout());
                    Action bestAction = search.makeDecision(state);
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
                    TablutCrewSearch search = new TablutCrewSearch(game, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, this.getTimeout());
                    Action bestAction = search.makeDecision(state);
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
