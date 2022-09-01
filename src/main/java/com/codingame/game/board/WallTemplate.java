package com.codingame.game.board;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WallTemplate {

    public static List<String> walls = Stream.of(
        "XX\nXX",
        "XXX",
        "XXXXX",
        "X..\nX..\nXXX",
        "XXX\nXXX\nXXX",
        "X..\nXX.\n.XX",
        "XXX\n.X.\n.X.").collect(Collectors.toList());

}
