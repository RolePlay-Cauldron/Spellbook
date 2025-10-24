package com.github.roleplaycauldron.spellbook.core;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Logger providing better logging methods as well as Debug logging
 */
public class WrappedLogger {

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

    /**
     * (De-)activated the Debug Mode for this Logger. See {@link WrappedLogger#debugF(String, Object...)}
     *
     * @param debug true activates debug mode, false deactivates debug mode
     * @see WrappedLogger#debugF(String, Object...)
     */
    public void setDebug(boolean debug) {
        this.isDebug = debug;
    }

    /**
     * A wrapper for {@link Logger#log(Level, String)} with {@link Level#INFO}
     * that calls {@link String#format(String, Object...)} for you
     *
     * @param message log message format
     * @param args arguments for {@link String#format(String, Object...)}
     */
    public void infoF(String message, Object... args) {
        logger.log(Level.INFO, String.format(message, args));
    }

    /**
     * A wrapper for {@link Logger#log(Level, String)} with {@link Level#WARNING}
     * that calls {@link String#format(String, Object...)} for you
     *
     * @param message log message format
     * @param args arguments for {@link String#format(String, Object...)}
     */
    public void warnF(String message, Object... args) {
        logger.log(Level.WARNING, String.format(message, args));
    }

    /**
     * A wrapper for {@link Logger#log(Level, String)} with {@link Level#SEVERE}
     * that calls {@link String#format(String, Object...)} for you
     *
     * @param message log message format
     * @param args arguments for {@link String#format(String, Object...)}
     */
    public void errorF(String message, Object... args) {
        logger.log(Level.SEVERE, String.format(message, args));
    }

    /**
     * A wrapper for {@link Logger#log(Level, String)} with {@link Level#INFO}
     * that calls {@link String#format(String, Object...)} for you<br>
     * <br>
     * Debug Logs are only send if {@link WrappedLogger#isDebug} is set to true, see {@link WrappedLogger#setDebug(boolean)}
     *
     * @param message log message format
     * @param args arguments for {@link String#format(String, Object...)}
     * @see WrappedLogger#setDebug(boolean)
     */
    public void debugF(String message, Object... args) {
        if (isDebug) {
            logger.log(Level.INFO, "[Debug] " + String.format(message, args));
        }
    }
}
