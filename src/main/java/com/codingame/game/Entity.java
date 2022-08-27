package com.codingame.game;

import com.codingame.gameengine.module.entities.Rectangle;

public class Entity {

    protected int owner;
    protected Type type;
    public Rectangle rectangle;

    public Entity(int owner, Type type) {
        this.owner = owner;
        this.type = type;
    }


    public Entity() {
        type = Type.EMPTY;
        owner = -1;
    }

    public void color(int color) {
        type = color == 1 ? Type.COLOR1 : Type.COLOR2;
        owner = color == 1 ? 0 : 1;
    }

    public int getOwner() {
        return this.owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public void switchOwner() {
        owner = owner == 1 ? 0 : 1;
    }

    public enum Type {
        EMPTY,
        WALL,
        COLOR1,
        COLOR2,
        PAWN
    }

    public static char typeToChar(Type type) {
        switch (type) {
            case EMPTY:
                return '.';
            case WALL:
                return 'X';
            case COLOR1:
                return '1';
            case COLOR2:
                return '2';
            default:
                return ' ';
        }
    }
}