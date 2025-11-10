package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline;

import it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.tavoletta.TavolettaPlayer;

import java.io.IOException;

public class BaselineWhitePlayer {

    /*
     * Ogni player dovrà prendere in input questi argomenti
     * RUOLO TIMEOUT IPSERVER NOME
     * */
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String[] arguments = new String[]{"WHITE", "60", "localhost", "BaselinePlayer"};

        if (args.length > 0) {
            arguments = new String[]{"WHITE", args[1], args[2], args[3]};
        }

        BaselinePlayer.main(arguments);
    }
}
