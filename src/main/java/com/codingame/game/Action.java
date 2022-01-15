package com.codingame.game;

public class Action {
    public final int pawn;
    public final int direction;
    public final int range;
    public Player player;
    public final ActionType type;
    
    public Action(Player player,ActionType type, int pawn, int col, int range) {
        this.player = player;
        this.pawn = pawn;
        this.direction = col;
        this.range = range;
        this.type=type;
    }
    
    @Override
    public String toString() {
        return pawn + " " + direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Action) {
            Action other = (Action) obj;
            return direction == other.direction && pawn == other.pawn;
        } else {
            return false;
        }
    }

    public enum ActionType{
        MOVE,
        SHOOT
    }
}