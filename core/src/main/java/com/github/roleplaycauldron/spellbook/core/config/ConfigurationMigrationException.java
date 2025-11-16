package com.github.roleplaycauldron.spellbook.core.config;

/**
 * This Exception describes Errors happening during Configuration Migration
 */
public class ConfigurationMigrationException extends RuntimeException {

    /**
     * Creates a new {@link ConfigurationMigrationException}
     * @param message the cause
     */
    public ConfigurationMigrationException(String message) {
        super(message);
    }

    /**
     * Like {@link ConfigurationMigrationException#ConfigurationMigrationException(String)}
     * but it calls {@link String#formatted(Object...)} on the message
     *
     * @param message the cause (as String format)
     * @param args the String format arguments
     */
    public ConfigurationMigrationException(String message, Object... args) {
        this(message.formatted(args));
    }
}
