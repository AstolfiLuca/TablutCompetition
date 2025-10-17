package it.unibo.ai.didattica.competition.tablut.crew.client;

import java.io.IOException;
import java.net.UnknownHostException;

// Nota: Si potrebbero usare anche i threads

public class WhiteCrewTablutClient {

	public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException {
		// role, name, timeout, ip, gameVariant, debug
		String[] arguments = new String[]{"WHITE", "Crew", "50", "localhost", "0", "debug"}; // default arguments

        if(args.length > 0) arguments = new String[]{"WHITE", args[0]}; // custom arguments

		CrewTablutClient client = new CrewTablutClient(arguments);

		Thread t = new Thread(client);

		t.start();
	}
}