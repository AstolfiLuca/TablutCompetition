package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.tavoletta;

import java.io.IOException;
import java.net.UnknownHostException;

public class TavolettaWhitePlayer {

    public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException {
        String[] arguments = new String[]{"WHITE", "60", "localhost", "debug"};

        if(args.length > 0) {
            arguments = new String[]{"WHITE", args[0]};
        }

        TavolettaPlayer.main(arguments);
    }
}
