package com.chapeullah.GucciGoblin.infrastructure;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GucciLogger {

    private final Logger logger;

    private GucciLogger(@NonNull Class<?> owner) {
        this.logger = LoggerFactory.getLogger(owner);
    }

    public static GucciLogger of(@NonNull Class<?> owner) {
        return new GucciLogger(owner);
    }

    public void info(@NonNull String message) {
        logger.info(message);
    }

    public void warn(@NonNull String message) {
        logger.warn(message);
    }

    public void error(@NonNull String message) {
        logger.error(message);
    }

    public void iterStart(long iter) {
        String left = String.format("ITER=%10d ", iter); // слева фикс. формат
        int fillCount = Math.max(0, 111 - left.length());
        System.out.printf("%s%s%n", left, String.valueOf("=").repeat(fillCount));
    }

}