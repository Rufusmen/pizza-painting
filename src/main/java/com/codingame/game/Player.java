package com.codingame.game;

import com.codingame.game.Action.ActionType;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Group;
import java.util.ArrayList;
import java.util.List;

/**
 * Player object.
 */
public class Player extends AbstractMultiplayerPlayer {
    public Group hud;
    /**
     * Number of units under player control.
     */
    public int pawns;
    
    @Override
    public int getExpectedOutputLines() {
        return pawns;
    }

    public void setPawns(int pawns){
        this.pawns = pawns;
    }

    /**
     * Parses player outputs to action objects.
     *
     * @return list of player's actions
     */
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
