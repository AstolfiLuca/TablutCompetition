package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline;

import java.io.IOException;

public class BaselineWhitePlayer {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String[] array = new String[]{"WHITE"};
        if (args.length>0){
            array = new String[]{"WHITE", args[0]};
        }
        BaselinePlayer.main(array);;
    }
}
