package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.tavoletta;

import java.io.IOException;
import java.net.UnknownHostException;

public class TavolettaBlackPlayer {

    public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException {
        String[] arguments = new String[]{"BLACK", "60", "localhost", "debug"};

        if(args.length > 0) {
            arguments = new String[]{"BLACK", args[0]};
        }

        TavolettaPlayer.main(arguments);
    }
}
