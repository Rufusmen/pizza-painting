package com.codingame.game.util;

import java.util.Arrays;
import java.util.stream.Stream;


/**
 * Custom implementation of Java's 11 lines function
 */
public class LineSeparator {

    public static Stream<String> lines(String s){
        return Arrays.stream(s.split("\n"));
    }

}
