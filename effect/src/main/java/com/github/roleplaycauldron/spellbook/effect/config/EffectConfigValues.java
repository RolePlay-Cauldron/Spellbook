package com.github.roleplaycauldron.spellbook.effect.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility methods for reading and validating effect configuration values.
 *
 * <p>This class centralizes common configuration parsing operations used by
 * {@link EffectConfigParser} and its component parsers. It provides helpers for
 * required and optional sections, typed primitive values, enum parsing, type
 * normalization, and path-aware exception handling.</p>
 */
final class EffectConfigValues {

    private EffectConfigValues() {
    }

    /**
     * Returns a required child configuration section.
     *
     * <p>If the value is stored as a map instead of a Bukkit
     * {@link ConfigurationSection}, it is converted into a temporary section.</p>
     *
     * @param parent parent configuration section
     * @param key    child key to read
     * @param path   configuration path used for error reporting
     * @return required child section
     * @throws EffectConfigException if the section is missing or not a section-like value
     */
    /* default */
    static ConfigurationSection requiredSection(ConfigurationSection parent, String key, String path) {
        ConfigurationSection section = optionalSection(parent, key, path);
        if (section == null) {
            throw new EffectConfigException(path, "Missing required section");
        }
        return section;
    }

    /**
     * Returns an optional child configuration section.
     *
     * <p>If the value is stored as a map instead of a Bukkit
     * {@link ConfigurationSection}, it is converted into a temporary section.
     * Missing values return {@code null}.</p>
     *
     * @param parent parent configuration section
     * @param key    child key to read
     * @param path   configuration path used for error reporting
     * @return optional child section, or {@code null} if absent
     * @throws EffectConfigException if the value exists but is not section-like
     */
    /* default */
    static ConfigurationSection optionalSection(ConfigurationSection parent, String key, String path) {
        ConfigurationSection section = parent.getConfigurationSection(key);
        if (section != null) {
            return section;
        }

        Object value = parent.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            return sectionFromMap(map, path);
        }

        throw new EffectConfigException(path, "Expected configuration section");
    }

    /**
     * Reads a list of configuration sections.
     *
     * <p>Each list element may either already be a {@link ConfigurationSection}
     * or a map that can be converted into one. Returned entries include indexed
     * paths such as {@code transforms[0]} for precise error reporting.</p>
     *
     * @param parent parent configuration section
     * @param key    list key to read
     * @param path   configuration path used for error reporting
     * @return list of indexed configuration sections, or an empty list if absent
     * @throws EffectConfigException if the value is not a list or contains non-section elements
     */
    /* default */
    static List<EffectConfigParser.IndexedSection> sectionList(ConfigurationSection parent, String key, String path) {
        if (!parent.contains(key)) {
            return List.of();
        }

        Object value = parent.get(key);
        if (!(value instanceof List<?> values)) {
            throw new EffectConfigException(path, "Expected list");
        }

        List<EffectConfigParser.IndexedSection> sections = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            String elementPath = path + "[" + i + "]";
            Object element = values.get(i);

            if (element instanceof ConfigurationSection section) {
                sections.add(new EffectConfigParser.IndexedSection(section, elementPath));
            } else if (element instanceof Map<?, ?> map) {
                sections.add(new EffectConfigParser.IndexedSection(sectionFromMap(map, elementPath), elementPath));
            } else {
                throw new EffectConfigException(elementPath, "Expected configuration section");
            }
        }

        return sections;
    }

    /**
     * Reads a required non-blank string.
     *
     * @param section configuration section
     * @param key     value key to read
     * @param path    configuration path used for error reporting
     * @return required string value
     * @throws EffectConfigException if the value is missing or blank
     */
    /* default */
    static String requireString(ConfigurationSection section, String key, String path) {
        String value = section.getString(key);
        if (value == null || value.isBlank()) {
            throw new EffectConfigException(path, "Missing required string");
        }
        return value;
    }

    /**
     * Reads a required integer.
     *
     * <p>Integral number types are accepted directly. Floating-point values are
     * accepted only when they are finite and represent an exact integer within
     * the {@code int} range.</p>
     *
     * @param section configuration section
     * @param key     value key to read
     * @param path    configuration path used for error reporting
     * @return required integer value
     * @throws EffectConfigException if the value is missing or not a valid integer
     */
    /* default */
    static int requireInt(ConfigurationSection section, String key, String path) {
        if (!section.contains(key)) {
            throw new EffectConfigException(path, "Missing required integer");
        }
        return requireIntegerValue(section.get(key), path);
    }

    /**
     * Reads an optional integer.
     *
     * @param section  configuration section
     * @param key      value key to read
     * @param path     configuration path used for error reporting
     * @param fallback value returned when the key is absent
     * @return configured integer value, or {@code fallback} if absent
     * @throws EffectConfigException if the value exists but is not a valid integer
     */
    /* default */
    static int getInt(ConfigurationSection section, String key, String path, int fallback) {
        return section.contains(key) ? requireIntegerValue(section.get(key), path) : fallback;
    }

    /**
     * Reads a required float.
     *
     * @param section configuration section
     * @param key     value key to read
     * @param path    configuration path used for error reporting
     * @return required finite float value
     * @throws EffectConfigException if the value is missing or not a finite number
     */
    /* default */
    static float requireFloat(ConfigurationSection section, String key, String path) {
        if (!section.contains(key)) {
            throw new EffectConfigException(path, "Missing required number");
        }
        return requireFloatValue(section.get(key), path);
    }

    /**
     * Reads an optional float.
     *
     * @param section  configuration section
     * @param key      value key to read
     * @param path     configuration path used for error reporting
     * @param fallback value returned when the key is absent
     * @return configured finite float value, or {@code fallback} if absent
     * @throws EffectConfigException if the value exists but is not a finite number
     */
    /* default */
    static float getFloat(ConfigurationSection section, String key, String path, float fallback) {
        return section.contains(key) ? requireFloatValue(section.get(key), path) : fallback;
    }

    /**
     * Reads a required double.
     *
     * @param section configuration section
     * @param key     value key to read
     * @param path    configuration path used for error reporting
     * @return required finite double value
     * @throws EffectConfigException if the value is missing or not a finite number
     */
    /* default */
    static double requireDouble(ConfigurationSection section, String key, String path) {
        if (!section.contains(key)) {
            throw new EffectConfigException(path, "Missing required number");
        }
        return requireDoubleValue(section.get(key), path);
    }

    /**
     * Reads an optional double.
     *
     * @param section  configuration section
     * @param key      value key to read
     * @param path     configuration path used for error reporting
     * @param fallback value returned when the key is absent
     * @return configured finite double value, or {@code fallback} if absent
     * @throws EffectConfigException if the value exists but is not a finite number
     */
    /* default */
    static double getDouble(ConfigurationSection section, String key, String path, double fallback) {
        return section.contains(key) ? requireDoubleValue(section.get(key), path) : fallback;
    }

    /**
     * Reads an optional boolean.
     *
     * @param section  configuration section
     * @param key      value key to read
     * @param path     configuration path used for error reporting
     * @param fallback value returned when the key is absent
     * @return configured boolean value, or {@code fallback} if absent
     * @throws EffectConfigException if the value exists but is not a boolean
     */
    /* default */
    static boolean getBoolean(ConfigurationSection section, String key, String path, boolean fallback) {
        if (!section.contains(key)) {
            return fallback;
        }

        Object value = section.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        throw new EffectConfigException(path, String.format("Expected boolean, got %s", typeName(value)));
    }

    /**
     * Parses an enum value from configuration text.
     *
     * <p>Values are matched case-insensitively after converting hyphens to
     * underscores, so configuration values such as {@code look-at} can match
     * enum constants such as {@code LOOK_AT}.</p>
     *
     * @param enumType enum class to parse
     * @param value    configuration value
     * @param path     configuration path used for error reporting
     * @param <E>      enum type
     * @return parsed enum constant
     * @throws EffectConfigException if the value does not match an enum constant
     */
    /* default */
    static <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String path) {
        try {
            return Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException exception) {
            throw new EffectConfigException(path, String.format("Unknown %s value '%s'", enumType.getSimpleName(), value), exception);
        }
    }

    /**
     * Normalizes a parser type name.
     *
     * <p>Normalization trims whitespace, converts the value to lower case, and
     * replaces underscores with hyphens.</p>
     *
     * @param type raw type name
     * @return normalized type name
     * @throws IllegalArgumentException if the type is {@code null} or blank
     */
    /* default */
    static String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be blank");
        }
        return type.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Attempts to derive a more precise configuration field path from an
     * {@link IllegalArgumentException}.
     *
     * <p>This helper is intended for exceptions thrown by builders or value
     * objects whose messages begin with a Java-style field name. For example,
     * a message starting with {@code offsetX} may be mapped to
     * {@code offset-x} when that field exists in the configuration section.</p>
     *
     * @param section   configuration section being parsed
     * @param path      current configuration path
     * @param exception exception to inspect
     * @return refined field path if one can be derived, otherwise {@code path}
     */
    /* default */
    static String fieldPathForException(ConfigurationSection section, String path, IllegalArgumentException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return path;
        }

        int firstSpace = message.indexOf(' ');
        if (firstSpace <= 0) {
            return path;
        }

        String field = camelToKebab(message.substring(0, firstSpace));
        if (section.contains(field)) {
            return path + "." + field;
        }

        return path;
    }

    /**
     * Converts a map into a temporary Bukkit configuration section.
     *
     * @param map  source map
     * @param path configuration path used for error reporting
     * @return configuration section containing the map entries
     * @throws EffectConfigException if any map key is not a string
     */
    private static ConfigurationSection sectionFromMap(Map<?, ?> map, String path) {
        MemoryConfiguration section = new MemoryConfiguration();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new EffectConfigException(path, "Map keys must be strings");
            }

            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                copyMap(section.createSection(key), nested, path + "." + key);
            } else {
                section.set(key, value);
            }
        }
        return section;
    }

    /**
     * Copies map entries into an existing configuration section.
     *
     * @param target target configuration section
     * @param map    source map
     * @param path   configuration path used for error reporting
     * @throws EffectConfigException if any map key is not a string
     */
    private static void copyMap(ConfigurationSection target, Map<?, ?> map, String path) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new EffectConfigException(path, "Map keys must be strings");
            }

            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                copyMap(target.createSection(key), nested, path + "." + key);
            } else {
                target.set(key, value);
            }
        }
    }

    /**
     * Validates and converts an object into an integer.
     *
     * @param value value to convert
     * @param path  configuration path used for error reporting
     * @return integer value
     * @throws EffectConfigException if the value is not an integer-compatible number
     */
    private static int requireIntegerValue(Object value, String path) {
        if (!(value instanceof Number number)) {
            throw new EffectConfigException(path, String.format("Expected integer, got %s", typeName(value)));
        }

        if (!(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)) {
            return integerFromFloatingValue(number.doubleValue(), path);
        }

        long longValue = number.longValue();
        if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
            throw new EffectConfigException(path, String.format("Expected integer, got %s", value));
        }

        return (int) longValue;
    }

    /**
     * Converts a floating-point value into an integer if it represents one exactly.
     *
     * @param value floating-point value
     * @param path  configuration path used for error reporting
     * @return integer value
     * @throws EffectConfigException if the value is not finite, not integral, or out of range
     */
    private static int integerFromFloatingValue(double value, String path) {
        if (!Double.isFinite(value)) {
            throw new EffectConfigException(path, String.format("Expected finite integer, got %s", value));
        }
        if (value % 1 != 0 || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new EffectConfigException(path, String.format("Expected integer, got %s", value));
        }
        return (int) value;
    }

    /**
     * Validates and converts an object into a finite float.
     *
     * @param value value to convert
     * @param path  configuration path used for error reporting
     * @return finite float value
     * @throws EffectConfigException if the value is not a finite number
     */
    private static float requireFloatValue(Object value, String path) {
        if (!(value instanceof Number number)) {
            throw new EffectConfigException(path, String.format("Expected number, got %s", typeName(value)));
        }

        double doubleValue = number.doubleValue();
        if (!Double.isFinite(doubleValue)) {
            throw new EffectConfigException(path, String.format("Expected finite number, got %f", doubleValue));
        }

        float floatValue = number.floatValue();
        if (!Float.isFinite(floatValue)) {
            throw new EffectConfigException(path, String.format("Expected finite number, got %f", doubleValue));
        }

        return floatValue;
    }

    /**
     * Validates and converts an object into a finite double.
     *
     * @param value value to convert
     * @param path  configuration path used for error reporting
     * @return finite double value
     * @throws EffectConfigException if the value is not a finite number
     */
    private static double requireDoubleValue(Object value, String path) {
        if (!(value instanceof Number number)) {
            throw new EffectConfigException(path, String.format("Expected number, got %s", typeName(value)));
        }

        double doubleValue = number.doubleValue();
        if (!Double.isFinite(doubleValue)) {
            throw new EffectConfigException(path, String.format("Expected finite number, got %f", doubleValue));
        }

        return doubleValue;
    }

    /**
     * Returns a human-readable type name for error messages.
     *
     * @param value value to describe
     * @return simple class name of the value, or {@code null} for {@code null}
     */
    private static String typeName(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName();
    }

    /**
     * Converts a camel-case field name to kebab case.
     *
     * @param value camel-case value
     * @return kebab-case value
     */
    private static String camelToKebab(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (Character.isUpperCase(character)) {
                result.append('-').append(Character.toLowerCase(character));
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }
}
