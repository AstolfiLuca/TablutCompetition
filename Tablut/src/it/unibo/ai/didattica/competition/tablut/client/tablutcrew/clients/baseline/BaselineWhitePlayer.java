package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline;

import java.io.IOException;

public class BaselineWhitePlayer {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String[] array = new String[]{"WHITE"};
        if (args.length == 0) {
            array = new String[]{"WHITE"};
        } else if (args.length == 1) {
            array = new String[]{"WHITE", args[0]}; // timeout
        } else {
            // Pass all args including weights
            array = new String[args.length + 1];
            array[0] = "WHITE";
            System.arraycopy(args, 0, array, 1, args.length);
        }

        BaselinePlayer.main(array);;
    }
}
