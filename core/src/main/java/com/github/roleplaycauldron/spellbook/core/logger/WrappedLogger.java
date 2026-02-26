package com.github.roleplaycauldron.spellbook.core.logger;

import java.util.logging.Level;

/**
 * Provides a standardized interface for logging with enhanced formatting capabilities and debug mode support.
 * This interface defines methods for logging at different levels (info, warning, error)
 * and includes a special method for debug logging which can be conditionally enabled.
 */
public interface WrappedLogger {

    /**
     * A wrapper for a Logger with {@link Level#INFO}
     * that calls {@link String#format(String, Object...)} for you
     *
     * @param message log message format
     * @param args    arguments for {@link String#format(String, Object...)}
     */
    void infoF(String message, Object... args);

    /**
     * A wrapper for a Logger with {@link Level#WARNING}
     * that calls {@link String#format(String, Object...)} for you
     *
     * @param message log message format
     * @param args    arguments for {@link String#format(String, Object...)}
     */
    void warnF(String message, Object... args);

    /**
     * A wrapper for a Logger with {@link Level#SEVERE}
     * that calls {@link String#format(String, Object...)} for you
     *
     * @param message log message format
     * @param args    arguments for {@link String#format(String, Object...)}
     */
    void errorF(String message, Object... args);

    /**
     * A wrapper for a Logger with {@link Level#INFO}
     * that calls {@link String#format(String, Object...)} for you<br>
     * <br>
     * Debug Logs are only sent if a custom debug flag is set to {@code true}, see {@link LoggerFactory#setDebug(boolean)}
     *
     * @param message log message format
     * @param args    arguments for {@link String#format(String, Object...)}
     * @see LoggerFactory#setDebug(boolean)
     */
    void debugF(String message, Object... args);
}
