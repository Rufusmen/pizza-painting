package com.codingame.game.util;

import java.util.Arrays;
import java.util.stream.Stream;

public class LineSeparator {

    public static Stream<String> lines(String s){
        return Arrays.stream(s.split("\n"));
    }

}
