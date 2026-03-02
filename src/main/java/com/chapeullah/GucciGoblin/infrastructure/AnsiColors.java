package com.chapeullah.GucciGoblin.infrastructure;

import lombok.NonNull;

public final class AnsiColors {

    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    private AnsiColors() {}

    public static String green(@NonNull String s) {
        return GREEN + s + RESET;
    }

    public static String yellow(@NonNull String s) {
        return YELLOW + s + RESET;
    }

    public static String red(@NonNull String s) {
        return RED + s + RESET;
    }
}