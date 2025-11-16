package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline;

import java.io.IOException;

public class BaselineBlackPlayer {
    public final static int REPEATED_MOVES_ALLOWED = 0;
    public final static int CACHED_MOVES_ALLOWED = -1;
    public final static String LOGS_FOLDER = "logs";

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String[] array;
        if (args.length == 0) {
            array = new String[]{"BLACK"};
        } else if (args.length == 1) {
            array = new String[]{"BLACK", args[0]}; // timeout
        } else {
            // Pass all args including weights
            array = new String[args.length + 1];
            array[0] = "BLACK";
            System.arraycopy(args, 0, array, 1, args.length);
        }
        // Instantiate via main
        BaselinePlayer.main(array);
    }
}
