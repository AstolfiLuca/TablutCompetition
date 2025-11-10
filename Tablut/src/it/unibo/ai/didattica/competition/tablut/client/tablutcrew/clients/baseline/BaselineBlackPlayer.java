package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients.baseline;

import java.io.IOException;

public class BaselineBlackPlayer {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String[] array = new String[]{"BLACK"};
        if (args.length>0){
            array = new String[]{"BLACK", args[0]};
        }
        BaselinePlayer.main(array);
    }
}
