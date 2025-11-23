package it.unibo.ai.didattica.competition.tablut.crew.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.Double;

import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
// import it.unibo.ai.didattica.competition.tablut.Crew.search.CrewGameAshtonTablut;
// import it.unibo.ai.didattica.competition.tablut.Crew.search.Player;

public class CrewTablutClient extends TablutClient {

	private int gameVariant;
	private boolean debug;
	private Player player;
	
	public CrewTablutClient(String role, String name, int timeout, String ipAddress, int gameVariant, boolean debug) throws UnknownHostException, IOException {
		super(role, name, timeout, ipAddress);
		this.gameVariant = gameVariant;
		this.debug = debug;
	}

	public CrewTablutClient(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, name, timeout, ipAddress, 0, false);
	}
	
	public CrewTablutClient(String player, String name, String ipAddress) throws UnknownHostException, IOException {
		this(player, name, 60, ipAddress, 0, false);
	}
	
	public CrewTablutClient(String player, String name) throws UnknownHostException, IOException {
		this(player, name, 60, "localhost", 0, false);
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		String teamName = "Crew";
		String role = ""; 				// args[0]
		int timeout = 60; 				// args[1]
		String ipAddress = "localhost"; // args[2]
		boolean debug = false; 			// args[3]
		
		// No arguments
		if (!args.length){
			System.err.println("You have to specify which role you want to play Black | White");
			System.exit(-1);
		}

		// Check role argument
		if(!(args[0].equalsIgnoreCase("black") || args[0].equalsIgnoreCase("white"))) {
			System.err.println("You have to specify which role you want to play Black | White");
			System.exit(-1);
		} 
		
		role = args[0];
		
		timeout = Integer.parseInt(args[1]);

		// Check timeout argument
		if(timeout <= 2) {
			System.err.println("Match timeout must be an integer number bigger than 2");
			System.exit(-1);
		}
              
        ipAddress = args[2];
		
		// Check debug argument
		if(args[3].equalsIgnoreCase("debug")) 
			debug = true;
		 else 
			System.err.println("Symbol \"" + args[3] + "\n not recognized, debug is now disabled"); // No Error

		// Too many arguments
		if(args.length > 4) {
			System.err.println("Too many arguments, expected: <role> [timeout] [ipAddress] [debug]");
			System.exit(-1);
		}

		// Print configuration
        System.out.println("Player:  " + role);
        System.out.println("Timeout: " + timeout + " secondi");
        System.out.println("Server:  " + ip);
        if(debug) System.out.println("Debug: Verbose Output activated" + deb + "\n");
		System.out.println("****************************************************************");
	
        CrewTablutClient client = new CrewTablutClient(role, teamName, timeout, ipAddress, 0, debug);

        client.run();
	}
	
	
	@Override
	public void run() {	
		State state = null;
		
		aima.core.search.adversarial.Game<State,Action,Turn> rules=null;
		
		if (gameVariant == 0){
			state = new StateTablut();
			rules = new CrewGameAshtonTablut(0, -1, "logs", "white_ai", "black_ai"); //FIXME non usiamo la classe AshtonTablutGame ma usiamo un adapter o altro
		} else{
			System.err.println("Game variant not recognized");
			System.exit(-1);
		}
		
		// Invia il nome
		try {
			declareName();	
		}catch(Exception e) {
			e.printStackTrace();
		}
				
		player = new Player(rules, Double.MIN_VALUE,Double.MAX_VALUE,super.getTimeout()-2,debug);
		
		state.setTurn(State.Turn.WHITE);
		
		// Game
        while(true) {
	        try {
	            this.read();
	        } catch (ClassNotFoundException | IOException e1) {
	            e1.printStackTrace();
	            System.exit(1);
	        }
	
	        // print current state
	        state = this.getCurrentState();
	        System.out.println("Current state: \n" + state.toString());
			
			Turn turn = this.getCurrentState().getTurn();
			
			switch(turn) {
				case WHITE:
				case BLACK:
					if (this.getPlayer().equals(turn)){ // if is my turn
						Action a = player.makeDecision(state); // Search the best move in search tree

						System.out.println(a);

						try {
							this.write(a);
						} catch (ClassNotFoundException | IOException e) {
							e.printStackTrace();
						}
					}
		
				// if WHITEWIN 
				case WHITEWIN:
					System.out.println(this.getPlayer().equals(State.Turn.WHITE) ? "YOU WON!" : "YOU LOSE!");
					System.exit(0);
					break;
				
		
				// if BLACKWIN
				case BLACKWIN:
					System.out.println(this.getPlayer().equals(State.Turn.BLACK) ? "YOU WON!" : "YOU LOSE!");
					System.exit(0);
					break;
		
				// if DRAW
				case DRAW:
					System.out.println("DRAW!");
				// if BLACKWIN
					System.exit(0);
					break;

				default:
					System.err.println("Turn not recognized");
					System.exit(-1);
	        }
	     
		}
	}
	
}