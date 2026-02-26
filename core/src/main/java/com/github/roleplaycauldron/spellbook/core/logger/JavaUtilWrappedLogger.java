package com.github.roleplaycauldron.spellbook.core.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A Logger providing better logging methods as well as Debug logging
 */
public class JavaUtilWrappedLogger implements WrappedLogger {

    /**
     * The underlying {@link Logger}
     */
    private final Logger logger;

    /**
     * Flag for debug mode
     */
    private boolean isDebug = false;

    /**
     * Creates a new {@link JavaUtilWrappedLogger}
     *
     * @param logger the {@link Logger} this Wrapper works on
     */
    public JavaUtilWrappedLogger(Logger logger, String topic) {
        this.logger = new TopicLogger(logger, this.getClass(), topic);
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

    /**
     * Represents a specialized logger that attaches an optional topic to log messages.
     * This class extends the {@link Logger} class and enhances its functionality by
     * prefixing log messages with a specified topic, allowing for more context-driven logging.
     * <p>
     * It serves as a subordinate logger to a parent {@link Logger} and includes initialization
     * logic to configure logging levels and hierarchical relationships to the parent logger.
     */
    private class TopicLogger extends Logger {
        /**
         * The topic of this logger.
         */
        private final String topic;

        /**
         * Creates a new {@link TopicLogger} that adds an optional topic.
         *
         * @param parentLogger A reference to the parent {@link Logger} which is used as parent for this logger.
         * @param clazz        The calling class.
         * @param topic        The topic to add or null.
         */
        public TopicLogger(@NotNull final Logger parentLogger, @NotNull final Class<?> clazz, @Nullable final String topic) {
            super(clazz.getCanonicalName(), null);
            try {
                initLogger(parentLogger);
            } catch (final WrappedLoggerException e) {
                parentLogger.log(Level.SEVERE, "Failed to initialize logger on creation.", e);
            }
            this.topic = topic == null ? "" : "(" + topic + ") ";
        }

        private void initLogger(final Logger parentLogger) throws WrappedLoggerException {
            try {
                setParent(parentLogger);
                setLevel(Level.ALL);
            } catch (final SecurityException e) {
                throw new WrappedLoggerException("Failed to initialize logger", e);
            }
        }

        @Override
        public void log(@NotNull final LogRecord logRecord) {
            logRecord.setMessage(topic + logRecord.getMessage());
            logRecord.setLoggerName(getName());
            super.log(logRecord);
        }
    }
}
