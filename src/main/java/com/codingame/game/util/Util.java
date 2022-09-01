package com.codingame.game.util;

public class Util {
    public static int convert(int orig, int cellSize, double unit) {
        return (int) (orig + unit * cellSize);
    }
}
