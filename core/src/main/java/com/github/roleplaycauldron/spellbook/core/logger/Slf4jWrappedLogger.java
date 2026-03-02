package com.github.roleplaycauldron.spellbook.core.logger;

import java.util.concurrent.atomic.*;

import org.slf4j.Logger;

/**
 * An implementation of the {@link WrappedLogger} interface, which provides logging functionality
 * using an SLF4J {@link Logger}. This class supports enhanced formatting and debug
 * mode control.
 */
public class Slf4jWrappedLogger implements WrappedLogger {

    /**
     * The empty format for the logger.
     */
    private static final String EMPTY_FORMAT = "{}{}";

    /**
     * The underlying {@link Logger}
     */
    private final Logger logger;

    /**
     * The topic of this logger
     */
    private final String topic;

    /**
     * Flag for debug mode
     */
    private final AtomicBoolean isDebug;

    /**
     * Creates a new {@link Slf4jWrappedLogger}
     *
     * @param logger  the {@link Logger} this Wrapper works on
     * @param topic   the topic of this logger
     * @param isDebug the debug flag
     */
    public Slf4jWrappedLogger(Logger logger, final String topic, AtomicBoolean isDebug) {
        this.logger = logger;
        this.topic = topic == null || topic.isEmpty() ? "" : "(" + topic + ") ";
        this.isDebug = isDebug;
    }

    @Override
    public void info(String message) {
        logger.info(EMPTY_FORMAT, topic, message);
    }

    @Override
    public void warn(String message) {
        logger.warn(EMPTY_FORMAT, topic, message);
    }

    @Override
    public void error(String message) {
        logger.error(EMPTY_FORMAT, topic, message);
    }

    @Override
    public void debug(String message) {
        if (isDebug.get()) {
            logger.info(EMPTY_FORMAT, topic, "[Debug] " + message);
        }
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.info(EMPTY_FORMAT, topic, message, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(EMPTY_FORMAT, topic, message, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(EMPTY_FORMAT, topic, message, throwable);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        if (isDebug.get()) {
            logger.info(EMPTY_FORMAT, topic, "[Debug] " + message);
        }
    }

    @Override
    public void infoF(String message, Object... args) {
        logger.info(EMPTY_FORMAT, topic, String.format(message, args));
    }

    @Override
    public void warnF(String message, Object... args) {
        logger.warn(EMPTY_FORMAT, topic, String.format(message, args));
    }

    @Override
    public void errorF(String message, Object... args) {
        logger.error(EMPTY_FORMAT, topic, String.format(message, args));
    }

    @Override
    public void debugF(String message, Object... args) {
        if (isDebug.get()) {
            logger.info(EMPTY_FORMAT, topic, "[Debug] " + String.format(message, args));
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebug.get();
    }
}
