package com.github.roleplaycauldron.spellbook.core.file;

/**
 * Represents an exception that is thrown to indicate errors during file operations.
 * This class extends {@link RuntimeException} and provides constructors to include
 * error messages and optional underlying causes.
 */
public class FileException extends RuntimeException {

    /**
     * Constructor wrapping the given Throwable with a {@link FileException}
     * @param message the message of this exception
     */
    public FileException(String message) {
        super(message);
    }

    /**
     * Constructor wrapping the given Throwable with a {@link FileException}
     * @param message the message of this exception
     */
    public FileException(String message, Throwable cause) {
        super(message, cause);
    }
}
