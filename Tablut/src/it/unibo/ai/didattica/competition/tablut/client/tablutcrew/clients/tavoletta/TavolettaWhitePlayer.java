package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.tavoletta;

import java.io.IOException;
import java.net.UnknownHostException;

public class TavolettaWhitePlayer {


    /*
     * Ogni player dovrà prendere in input questi argomenti
     * RUOLO TIMEOUT IPSERVER NOME
     * */
    public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException {
        String[] arguments = new String[]{"WHITE", "60", "localhost", "TavolettaPlayer"};

        if (args.length > 0) {
            arguments = new String[]{"WHITE", args[1], args[2], args[3]};
        }

        TavolettaPlayer.main(arguments);
    }
}
