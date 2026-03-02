package com.chapeullah.GucciGoblin.infrastructure;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GucciLogger {

    private final Logger logger;
    private static final Logger PLAIN = LoggerFactory.getLogger("PLAIN");

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

}