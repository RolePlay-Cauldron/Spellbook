package com.github.roleplaycauldron.spellbook.core.logger;

/**
 * Represents an exception that wraps another throwable to provide additional information
 * or context in the context of logging operations.
 * This exception is primarily used in scenarios where a logging-related error needs to be
 * encapsulated and rethrown with a more descriptive message.
 */
public class WrappedLoggerException extends RuntimeException {

    /**
     * Constructor wrapping the given Throwable with a {@link WrappedLoggerException}
     * @param message the message of this exception
     */
    public WrappedLoggerException(String message) {
        super(message);
    }

    /**
     * Constructor wrapping the given Throwable with a {@link WrappedLoggerException}
     * @param message the message of this exception
     */
    public WrappedLoggerException(String message, Throwable cause) {
        super(message, cause);
    }
}
