package com.github.roleplaycauldron.spellbook.core.logger;

import java.util.logging.Level;

/**
 * Provides a standardized interface for logging with enhanced formatting capabilities and debug mode support.
 * This interface defines methods for logging at different levels (info, warning, error)
 * and includes a special method for debug logging which can be conditionally enabled.
 */
public interface WrappedLogger {

    /**
     * Logs a message at the "info" level.
     *
     * @param message the message to log
     */
    void info(String message);

    /**
     * Logs a message at the "warning" level.
     *
     * @param message the message to log
     */
    void warn(String message);

    /**
     * Logs a message at the "error" level.
     *
     * @param message the message to log
     */
    void error(String message);

    /**
     * Logs a message at the "debug" level.
     * <p>
     * Debug Logs are only sent if a custom debug flag is set to {@code true}, see {@link LoggerFactory#setDebug(boolean)}
     *
     * @param message the message to log
     * @see LoggerFactory#setDebug(boolean)
     */
    void debug(String message);

    /**
     * Logs a message and an associated {@link Throwable} at the "info" level.
     *
     * @param message   the message to log
     * @param throwable the throwable to attach to the log entry
     */
    void info(String message, Throwable throwable);

    /**
     * Logs a message and an associated {@link Throwable} at the "warning" level.
     *
     * @param message   the message to log
     * @param throwable the throwable to attach to the log entry
     */
    void warn(String message, Throwable throwable);

    /**
     * Logs a message and an associated {@link Throwable} at the "error" level.
     *
     * @param message   the message to log
     * @param throwable the throwable to attach to the log entry
     */
    void error(String message, Throwable throwable);

    /**
     * Logs a message and an associated {@link Throwable} at the "debug" level.
     * <p>
     * Debug Logs are only sent if a custom debug flag is set to {@code true}, see {@link LoggerFactory#setDebug(boolean)}
     *
     * @param message   the message to log
     * @param throwable the throwable to attach to the log entry
     * @see LoggerFactory#setDebug(boolean)
     */
    void debug(String message, Throwable throwable);

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

    /**
     * Returns whether debug logging is enabled for this logger.
     *
     * @return true if debug logging is enabled, false otherwise
     */
    boolean isDebugEnabled();
}
