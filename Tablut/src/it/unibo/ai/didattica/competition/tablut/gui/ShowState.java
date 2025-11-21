package it.unibo.ai.didattica.competition.tablut.gui;

import com.google.gson.Gson;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;


public class ShowState {

    private static final Gui gui= new Gui(4);
    private static final Gson gson = new Gson();
    public static void main(String[] args) {
        String stateJson = "{\"board\":[[\"EMPTY\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"BLACK\",\"EMPTY\",\"EMPTY\",\"EMPTY\"],[\"EMPTY\",\"EMPTY\",\"EMPTY\",\"BLACK\",\"BLACK\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"EMPTY\"],[\"EMPTY\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"WHITE\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"EMPTY\"],[\"BLACK\",\"EMPTY\",\"BLACK\",\"EMPTY\",\"WHITE\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"BLACK\"],[\"BLACK\",\"BLACK\",\"WHITE\",\"WHITE\",\"KING\",\"WHITE\",\"EMPTY\",\"BLACK\",\"BLACK\"],[\"BLACK\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"WHITE\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"BLACK\"],[\"EMPTY\",\"EMPTY\",\"WHITE\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"WHITE\",\"EMPTY\",\"EMPTY\"],[\"EMPTY\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"BLACK\",\"EMPTY\",\"EMPTY\",\"EMPTY\",\"EMPTY\"],[\"EMPTY\",\"EMPTY\",\"EMPTY\",\"BLACK\",\"BLACK\",\"BLACK\",\"EMPTY\",\"EMPTY\",\"EMPTY\"]],\"turn\":\"BLACK\"}";
        State currentState = gson.fromJson(stateJson, StateTablut.class);
        gui.updateWithTitle(currentState);

    }
}
