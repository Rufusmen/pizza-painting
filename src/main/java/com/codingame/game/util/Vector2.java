package com.codingame.game.util;

public class Vector2 implements Cloneable{
    private int x;
    private int y;

    public Vector2() {
        this.x = -1;
        this.y = -1;
    }

    @Override
    public Vector2 clone() {
        try {
            return (Vector2) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Vector2(x,y);
        }
    }

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public void add(int x, int y) { this.x += x; this.y += y;}
    public void add(Vector2 v) { this.x += v.x; this.y += v.y;}
    public void sub(int x, int y) { this.x -= x; this.y -= y;}
    public void sub(Vector2 v) { this.x -= v.x; this.y -= v.y;}
    public void abs() {this.x = Math.abs(x); this.y = Math.abs(y);}
}