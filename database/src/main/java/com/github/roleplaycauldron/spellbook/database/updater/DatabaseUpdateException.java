package com.github.roleplaycauldron.spellbook.database.updater;

/**
 * This Exception gets thrown when a critical Error occurs during Database Version Migration
 */
public class DatabaseUpdateException extends RuntimeException {

    /**
     * Constructor wrapping the given Throwable with a {@link DatabaseUpdateException}
     * @param message the message of this exception
     * @param cause the cause of this exception
     */
    public DatabaseUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
