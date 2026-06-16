package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.DirectionProvider;
import com.github.roleplaycauldron.spellbook.effect.EffectModifier;
import com.github.roleplaycauldron.spellbook.effect.emitter.StandardParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.transform.Transform;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Context passed to component parsers.
 * <p>
 * The context exposes the current configuration path, nested parse operations,
 * and common field validation helpers. Component parsers should use it when
 * parsing child components or required values, so nested failures retain useful
 * paths and produce consistent error details.
 */
public final class EffectConfigContext {

    private final EffectConfigParser parser;

    private final String path;

    EffectConfigContext(EffectConfigParser parser, String path) {
        this.parser = parser;
        this.path = path;
    }

    /**
     * Returns the configuration path represented by this context.
     *
     * @return configuration path
     */
    public String path() {
        return path;
    }

    /**
     * Returns a child path below this context.
     *
     * @param child child key or index
     * @return full child path
     */
    public String path(String child) {
        if (path == null || path.isBlank()) {
            return child;
        }
        if (child.startsWith("[")) {
            return path + child;
        }
        return path + "." + child;
    }

    /**
     * Returns a context scoped to a child key or index below this context.
     *
     * @param child child key or index
     * @return child parser context
     */
    public EffectConfigContext child(String child) {
        return new EffectConfigContext(parser, path(child));
    }

    /**
     * Parses this context's section as a shape using the same parser registries.
     *
     * @param section shape section at this context path
     * @return parsed shape
     */
    public Shape parseShape(ConfigurationSection section) {
        return parseShape(section, path);
    }

    /**
     * Parses a nested shape section using the same parser registries.
     *
     * @param section nested shape section
     * @param path    nested configuration path
     * @return parsed shape
     */
    public Shape parseShape(ConfigurationSection section, String path) {
        return parser.parseShape(section, path);
    }

    /**
     * Parses this context's section as a transform using the same parser registries.
     *
     * @param section transform section at this context path
     * @return parsed transform
     */
    public Transform parseTransform(ConfigurationSection section) {
        return parseTransform(section, path);
    }

    /**
     * Parses a nested transform section using the same parser registries.
     *
     * @param section nested transform section
     * @param path    nested configuration path
     * @return parsed transform
     */
    public Transform parseTransform(ConfigurationSection section, String path) {
        return parser.parseTransform(section, path);
    }

    /**
     * Parses this context's section as a modifier using the same parser registries.
     *
     * @param section modifier section at this context path
     * @return parsed modifier
     */
    public EffectModifier parseModifier(ConfigurationSection section) {
        return parseModifier(section, path);
    }

    /**
     * Parses a nested modifier section using the same parser registries.
     *
     * @param section nested modifier section
     * @param path    nested configuration path
     * @return parsed modifier
     */
    public EffectModifier parseModifier(ConfigurationSection section, String path) {
        return parser.parseModifier(section, path);
    }

    /**
     * Parses this context's section as a direction provider using the same parser registries.
     *
     * @param section direction section at this context path
     * @return parsed direction provider
     */
    public DirectionProvider parseDirection(ConfigurationSection section) {
        return parseDirection(section, path);
    }

    /**
     * Parses a nested direction section using the same parser registries.
     *
     * @param section nested direction section
     * @param path    nested configuration path
     * @return parsed direction provider
     */
    public DirectionProvider parseDirection(ConfigurationSection section, String path) {
        return parser.parseDirection(section, path);
    }

    /**
     * Parses this context's section as a standard particle section.
     *
     * @param section particle section at this context path
     * @return parsed standard particle emitter
     */
    public StandardParticleEmitter<Object> parseParticle(ConfigurationSection section) {
        return parseParticle(section, path);
    }

    /**
     * Parses a nested standard particle section.
     *
     * @param section nested particle section
     * @param path    nested configuration path
     * @return parsed standard particle emitter
     */
    public StandardParticleEmitter<Object> parseParticle(ConfigurationSection section, String path) {
        return parser.parseParticle(section, path);
    }

    /**
     * Returns a required child configuration section.
     *
     * @param parent parent section
     * @param key    child key
     * @return child section
     */
    public ConfigurationSection requireSection(ConfigurationSection parent, String key) {
        return EffectConfigValues.requiredSection(parent, key, path(key));
    }

    /**
     * Returns an optional child configuration section.
     *
     * @param parent parent section
     * @param key    child key
     * @return child section, or {@code null} when absent
     */
    public ConfigurationSection optionalSection(ConfigurationSection parent, String key) {
        return EffectConfigValues.optionalSection(parent, key, path(key));
    }

    /**
     * Returns an optional list of child configuration sections.
     *
     * @param parent parent section
     * @param key    list key
     * @return indexed child sections
     */
    public List<EffectConfigParser.IndexedSection> sectionList(ConfigurationSection parent, String key) {
        return EffectConfigValues.sectionList(parent, key, path(key));
    }

    /**
     * Reads a required non-blank string.
     *
     * @param section configuration section
     * @param key     value key
     * @return configured string
     */
    public String requireString(ConfigurationSection section, String key) {
        return EffectConfigValues.requireString(section, key, path(key));
    }

    /**
     * Reads a required integer.
     *
     * @param section configuration section
     * @param key     value key
     * @return configured integer
     */
    public int requireInt(ConfigurationSection section, String key) {
        return EffectConfigValues.requireInt(section, key, path(key));
    }

    /**
     * Reads a required float-compatible number.
     *
     * @param section configuration section
     * @param key     value key
     * @return configured float
     */
    public float requireFloat(ConfigurationSection section, String key) {
        return EffectConfigValues.requireFloat(section, key, path(key));
    }

    /**
     * Reads a required double-compatible number.
     *
     * @param section configuration section
     * @param key     value key
     * @return configured double
     */
    public double requireDouble(ConfigurationSection section, String key) {
        return EffectConfigValues.requireDouble(section, key, path(key));
    }

    /**
     * Reads an optional float-compatible number.
     *
     * @param section  configuration section
     * @param key      value key
     * @param fallback value returned when absent
     * @return configured or fallback float
     */
    public float getFloat(ConfigurationSection section, String key, float fallback) {
        return EffectConfigValues.getFloat(section, key, path(key), fallback);
    }

    /**
     * Reads an optional boolean.
     *
     * @param section  configuration section
     * @param key      value key
     * @param fallback value returned when absent
     * @return configured or fallback boolean
     */
    public boolean getBoolean(ConfigurationSection section, String key, boolean fallback) {
        return EffectConfigValues.getBoolean(section, key, path(key), fallback);
    }

    /**
     * Parses an enum value using the same case-insensitive and dash-tolerant
     * rules as the default parser.
     *
     * @param enumType enum type
     * @param value    configured value
     * @param key      value key
     * @param <E>      enum type
     * @return parsed enum constant
     */
    public <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String key) {
        return EffectConfigValues.parseEnum(enumType, value, path(key));
    }

    /**
     * Normalizes a configured type name.
     *
     * @param type configured type
     * @return normalized type
     */
    public String normalizeType(String type) {
        return EffectConfigValues.normalizeType(type);
    }
}
