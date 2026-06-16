package com.github.roleplaycauldron.spellbook.effect.config;

/**
 * Thrown when an effect configuration section cannot be parsed.
 * <p>
 * The failed configuration path and detail message are exposed separately, so
 * callers can report actionable errors to plugin users without parsing the
 * rendered exception message.
 */
public class EffectConfigException extends RuntimeException {

    private final String path;

    private final String detail;

    /**
     * Creates a new configuration exception.
     *
     * @param path   configuration path that failed
     * @param detail failure details
     */
    public EffectConfigException(String path, String detail) {
        super(formatMessage(path, detail));
        this.path = path;
        this.detail = detail;
    }

    /**
     * Creates a new configuration exception with a cause.
     *
     * @param path   configuration path that failed
     * @param detail failure details
     * @param cause  original failure
     */
    public EffectConfigException(String path, String detail, Throwable cause) {
        super(formatMessage(path, detail), cause);
        this.path = path;
        this.detail = detail;
    }

    private static String formatMessage(String path, String detail) {
        return String.format("%s: %s", path, detail);
    }

    /**
     * Returns the configuration path that failed.
     *
     * @return failed configuration path
     */
    public String path() {
        return path;
    }

    /**
     * Returns the failure detail without the configuration path prefix.
     *
     * @return failure detail
     */
    public String detail() {
        return detail;
    }
}
