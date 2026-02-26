package com.github.roleplaycauldron.spellbook.core.logger;

/**
 * A factory class for creating instances of {@link WrappedLogger}.
 * It supports either SLF4J or Java Util Logging implementations as the underlying logging framework.
 * The created {@link WrappedLogger} instances allow for standardized logging with enhanced features
 * such as formatted messages and optional debug mode.
 */
public class LoggerFactory {

    private java.util.logging.Logger javaUtilLogger;

    private org.slf4j.Logger slf4jLogger;

    /**
     * Constructs a LoggerFactory using the specified Java Util Logging logger.
     * This constructor allows for the initialization of the factory with a
     * Java Util Logging {@link java.util.logging.Logger} instance, which will
     * be used for creating {@link WrappedLogger} objects.
     *
     * @param javaUtilLogger the Java Util Logging {@link java.util.logging.Logger}
     *                       instance to be used by the LoggerFactory
     */
    public LoggerFactory(java.util.logging.Logger javaUtilLogger) {
        this.javaUtilLogger = javaUtilLogger;
    }

    /**
     * Constructs a LoggerFactory using the specified SLF4J logger.
     * This constructor initializes the factory with a SLF4J {@link org.slf4j.Logger}
     * instance, which will be used for creating {@link WrappedLogger} objects.
     *
     * @param slf4JLogger the SLF4J {@link org.slf4j.Logger} instance to be used by the LoggerFactory
     */
    public LoggerFactory(org.slf4j.Logger slf4JLogger) {
        this.slf4jLogger = slf4JLogger;
    }

    /**
     * Creates a new {@link WrappedLogger} instance for the specified class. The underlying logging
     * implementation used will depend on the configuration of the {@link LoggerFactory}. If no
     * specific topic is provided, the logger will use a default configuration.
     *
     * @param clazz the class for which the logger is being created
     * @return a new instance of {@link WrappedLogger} configured for the specified class
     */
    public WrappedLogger create(final Class<?> clazz) {
        return create(clazz, null);
    }

    /**
     * Creates a new {@link WrappedLogger} instance for the specified class and topic. The method selects the
     * appropriate logging implementation based on the configuration of the {@link LoggerFactory}. If neither
     * SLF4J nor Java Util Logging is available, an exception is thrown.
     *
     * @param clazz the class for which the logger is being created
     * @param topic the topic to associate with the logger, used for categorizing log messages
     * @return a new instance of {@link WrappedLogger} configured for the specified class and topic
     * @throws WrappedLoggerException if no supported logging implementation is available
     */
    public WrappedLogger create(final Class<?> clazz, final String topic) {
        if (slf4jLogger != null) {
            return new Slf4jWrappedLogger(slf4jLogger, topic);
        } else if (javaUtilLogger != null) {
            return new JavaUtilWrappedLogger(javaUtilLogger, topic);
        } else {
            throw new WrappedLoggerException("Unable to create logger: No SLF4J or Java Util Logger available");
        }
    }
}
