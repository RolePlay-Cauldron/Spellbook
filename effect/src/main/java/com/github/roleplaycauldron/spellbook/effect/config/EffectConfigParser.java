package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.DirectionProvider;
import com.github.roleplaycauldron.spellbook.effect.EffectBuilder;
import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import com.github.roleplaycauldron.spellbook.effect.EffectModifier;
import com.github.roleplaycauldron.spellbook.effect.emitter.StandardParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.transform.Transform;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Parses Bukkit configuration sections into Spellbook effect definitions.
 *
 * <p>A parser instance owns explicit registries for shapes, transforms,
 * modifiers, direction providers, and particle data. Use {@link #defaults()}
 * to create an independent parser with Spellbook's built-in component parsers
 * registered, then add plugin-specific parsers with the registration methods.</p>
 *
 * <p>Default configuration uses explicit {@code type} keys for polymorphic
 * components. Supported default shape types are {@code line}, {@code sphere},
 * {@code cube}, {@code helix}, {@code spiral-helix}, {@code moving-point}, and
 * {@code morph}. Supported default transform types are {@code translate},
 * {@code rotate}, and {@code look-at}. Supported standard particle fields are
 * {@code type}, {@code count}, {@code offset-x}, {@code offset-y},
 * {@code offset-z}, and {@code extra}. Advanced particle data is parsed only
 * through registered {@link ParticleDataConfigParser particle data parsers}.</p>
 */
public final class EffectConfigParser {

    private final EffectConfigRegistry registry;

    private final ParticleConfigParser particleParser;

    /**
     * Creates an empty parser with no registered component parsers.
     *
     * <p>Use this constructor when all supported component parsers should be
     * registered manually. Use {@link #defaults()} when the built-in Spellbook
     * parsers should be available immediately.</p>
     */
    public EffectConfigParser() {
        this.registry = new EffectConfigRegistry();
        this.particleParser = new ParticleConfigParser(this, registry);
    }

    /**
     * Creates an independent parser with Spellbook's default component parsers.
     *
     * @return parser with built-in shapes, transforms, directions, modifiers,
     * particle data parsers, and standard particle parsing
     */
    public static EffectConfigParser defaults() {
        EffectConfigParser parser = new EffectConfigParser();
        DefaultEffectConfigParsers.registerAll(parser);
        return parser;
    }

    /**
     * Registers a named shape parser.
     *
     * @param type   configuration type name
     * @param parser parser to invoke for that type
     * @return this parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    public EffectConfigParser registerShape(String type, ShapeConfigParser parser) {
        registry.registerShape(type, parser);
        return this;
    }

    /**
     * Registers a named transform parser.
     *
     * @param type   configuration type name
     * @param parser parser to invoke for that type
     * @return this parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    public EffectConfigParser registerTransform(String type, TransformConfigParser parser) {
        registry.registerTransform(type, parser);
        return this;
    }

    /**
     * Registers a named modifier parser.
     *
     * @param type   configuration type name
     * @param parser parser to invoke for that type
     * @return this parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    public EffectConfigParser registerModifier(String type, ModifierConfigParser parser) {
        registry.registerModifier(type, parser);
        return this;
    }

    /**
     * Registers a named direction provider parser.
     *
     * @param type   configuration type name
     * @param parser parser to invoke for that type
     * @return this parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    public EffectConfigParser registerDirection(String type, DirectionConfigParser parser) {
        registry.registerDirection(type, parser);
        return this;
    }

    /**
     * Registers a named advanced particle data parser.
     *
     * @param type   configuration data type name
     * @param parser parser to invoke for that data type
     * @return this parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    public EffectConfigParser registerParticleData(String type, ParticleDataConfigParser parser) {
        registry.registerParticleData(type, parser);
        return this;
    }

    /**
     * Parses an effect builder from a whole effect configuration section.
     *
     * <p>The configuration must contain at least a {@code shape} section and a
     * {@code particle} section. Optional {@code transforms}, {@code modifiers},
     * and {@code direction} sections are applied when present.</p>
     *
     * @param section effect configuration section
     * @return configured effect builder
     * @throws NullPointerException  if the section is {@code null}
     * @throws EffectConfigException if the section is invalid
     */
    public EffectBuilder parseBuilder(ConfigurationSection section) {
        Objects.requireNonNull(section, "section");

        EffectBuilder builder = EffectBuilder.create();
        builder.shape(parseShape(EffectConfigValues.requiredSection(section, "shape", "shape"), "shape"));
        builder.particle(parseParticle(EffectConfigValues.requiredSection(section, "particle", "particle"), "particle"));

        for (IndexedSection transform : EffectConfigValues.sectionList(section, "transforms", "transforms")) {
            builder.transform(parseTransform(transform.section(), transform.path()));
        }
        for (IndexedSection modifier : EffectConfigValues.sectionList(section, "modifiers", "modifiers")) {
            builder.modifier(parseModifier(modifier.section(), modifier.path()));
        }

        ConfigurationSection direction = EffectConfigValues.optionalSection(section, "direction", "direction");
        if (direction != null) {
            builder.direction(parseDirection(direction, "direction"));
        }
        return builder;
    }

    /**
     * Parses and builds an effect instance from a whole effect configuration section.
     *
     * @param section effect configuration section
     * @return configured effect instance
     * @throws NullPointerException  if the section is {@code null}
     * @throws EffectConfigException if the section is invalid
     */
    public EffectInstance parse(ConfigurationSection section) {
        return parseBuilder(section).build();
    }

    /**
     * Parses a shape from a typed shape configuration section.
     *
     * @param section shape configuration section
     * @param path    configuration path used for error reporting
     * @return parsed shape
     * @throws EffectConfigException if the section is invalid or the type is unknown
     */
    /* default */
    Shape parseShape(ConfigurationSection section, String path) {
        return parseTyped(section, path, "shape", registry::shapeParser,
                registry.knownShapeTypes(), parser -> parser.parse(section, new EffectConfigContext(this, path))
        );
    }

    /**
     * Parses a transform from a typed transform configuration section.
     *
     * @param section transform configuration section
     * @param path    configuration path used for error reporting
     * @return parsed transform
     * @throws EffectConfigException if the section is invalid or the type is unknown
     */
    /* default */
    Transform parseTransform(ConfigurationSection section, String path) {
        return parseTyped(section, path, "transform", registry::transformParser,
                registry.knownTransformTypes(), parser -> parser.parse(section, new EffectConfigContext(this, path))
        );
    }

    /**
     * Parses an effect modifier from a typed modifier configuration section.
     *
     * @param section modifier configuration section
     * @param path    configuration path used for error reporting
     * @return parsed effect modifier
     * @throws EffectConfigException if the section is invalid or the type is unknown
     */
    /* default */
    EffectModifier parseModifier(ConfigurationSection section, String path) {
        return parseTyped(section, path, "modifier", registry::modifierParser,
                registry.knownModifierTypes(), parser -> parser.parse(section, new EffectConfigContext(this, path))
        );
    }

    /**
     * Parses a direction provider from a typed direction configuration section.
     *
     * @param section direction configuration section
     * @param path    configuration path used for error reporting
     * @return parsed direction provider
     * @throws EffectConfigException if the section is invalid or the type is unknown
     */
    /* default */
    DirectionProvider parseDirection(ConfigurationSection section, String path) {
        return parseTyped(section, path, "direction", registry::directionParser,
                registry.knownDirectionTypes(), parser -> parser.parse(section, new EffectConfigContext(this, path))
        );
    }

    /**
     * Parses a standard particle emitter from a particle configuration section.
     *
     * @param section particle configuration section
     * @param path    configuration path used for error reporting
     * @return parsed particle emitter
     * @throws EffectConfigException if the section is invalid
     */
    /* default */
    StandardParticleEmitter<Object> parseParticle(ConfigurationSection section, String path) {
        return particleParser.parse(section, path);
    }

    /**
     * Parses a typed component by reading its {@code type} field, resolving the
     * matching registered parser, and invoking it.
     *
     * @param section      typed component configuration section
     * @param path         configuration path used for error reporting
     * @param component    human-readable component name used in error messages
     * @param parserLookup parser lookup function
     * @param knownTypes   registered type names used in unknown-type error messages
     * @param invoker      parser invocation function
     * @param <P>          parser type
     * @param <T>          parsed component type
     * @return parsed component
     * @throws EffectConfigException if the type is missing, unknown, or parsing fails
     */
    private <P, T> T parseTyped(
            ConfigurationSection section,
            String path,
            String component,
            Function<String, P> parserLookup,
            Set<String> knownTypes,
            Function<P, T> invoker
    ) {
        String type = EffectConfigValues.requireString(section, "type", String.format("%s.type", path));
        P parser = parserLookup.apply(type);
        if (parser == null) {
            throw new EffectConfigException(String.format("%s.type", path),
                    String.format("Unknown %s type '%s'. Registered types: %s", component, type, knownTypes));
        }
        try {
            return invoker.apply(parser);
        } catch (EffectConfigException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new EffectConfigException(EffectConfigValues.fieldPathForException(section, path, exception),
                    exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            throw new EffectConfigException(path, String.format("Failed to parse %s", component), exception);
        }
    }

    /**
     * A configuration section paired with its indexed configuration path.
     *
     * <p>This is used for list-like configuration entries so that parsed
     * elements can report precise error paths such as {@code transforms[0]}.</p>
     *
     * @param section configuration section
     * @param path    indexed section path
     */
    public record IndexedSection(ConfigurationSection section, String path) {
    }
}