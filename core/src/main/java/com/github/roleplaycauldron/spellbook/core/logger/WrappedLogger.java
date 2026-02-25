package com.github.roleplaycauldron.spellbook.core.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Logger providing better logging methods as well as Debug logging
 */
public class WrappedLogger implements WrappedLoggerInterface {

    /**
     * The underlying {@link Logger}
     */
    private final Logger logger;

    /**
     * Flag for debug mode
     */
    private boolean isDebug = false;

    /**
     * Creates a new {@link WrappedLogger}
     *
     * @param logger the {@link Logger} this Wrapper works on
     */
    public WrappedLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setDebug(boolean debug) {
        this.isDebug = debug;
    }

    @Override
    public void infoF(String message, Object... args) {
        logger.log(Level.INFO, String.format(message, args));
    }

    @Override
    public void warnF(String message, Object... args) {
        logger.log(Level.WARNING, String.format(message, args));
    }

    @Override
    public void errorF(String message, Object... args) {
        logger.log(Level.SEVERE, String.format(message, args));
    }

    @Override
    public void debugF(String message, Object... args) {
        if (isDebug) {
            logger.log(Level.INFO, "[Debug] " + String.format(message, args));
        }
    }
}
