package com.codingame.game;

import com.codingame.game.Action.ActionType;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Group;
import java.util.ArrayList;
import java.util.List;

public class Player extends AbstractMultiplayerPlayer {
    public Group hud;
    public int pawns;
    
    @Override
    public int getExpectedOutputLines() {
        return pawns;
    }

    public void setPawns(int pawns){
        this.pawns = pawns;
    }

    public List<Action> getActions() throws TimeoutException, NumberFormatException {
        List<Action> actions = new ArrayList<>();
        for (String output: getOutputs()
        ) {
            String[] outputs = output.split(" ");
            ActionType type = ActionType.valueOf(outputs[0]);
            actions.add(new Action(this,type, Integer.parseInt(outputs[1]), Integer.parseInt(outputs[2]), type == ActionType.SHOOT ? Integer.parseInt(outputs[3]) : 0));
        }
        return actions;
    }
}
