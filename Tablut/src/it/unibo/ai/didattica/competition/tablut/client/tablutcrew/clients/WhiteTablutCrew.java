package it.unibo.ai.didattica.competition.tablut.client.tablutcrew.clients;

import java.io.IOException;

public class WhiteTablutCrew {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String[] array = new String[]{"WHITE"};
        if (args.length>0){
            array = new String[]{"WHITE", args[0]};
        }
        TablutCrew.main(array);;
    }
}
