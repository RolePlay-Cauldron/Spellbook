package com.github.roleplaycauldron.spellbook.effect.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Registry for effect configuration parsers.
 *
 * <p>Stores parser instances grouped by their configuration category, such as shapes,
 * transforms, modifiers, directions, and particle data. Parser types are normalized before
 * they are stored or looked up.</p>
 */
final class EffectConfigRegistry {

    private final Map<String, ShapeConfigParser> shapeParsers;

    private final Map<String, TransformConfigParser> transformParsers;

    private final Map<String, ModifierConfigParser> modifierParsers;

    private final Map<String, DirectionConfigParser> directionParsers;

    private final Map<String, ParticleDataConfigParser> particleDataParsers;

    /**
     * Creates a new empty effect configuration parser registry.
     */
    EffectConfigRegistry() {
        this.shapeParsers = new LinkedHashMap<>();
        this.transformParsers = new LinkedHashMap<>();
        this.modifierParsers = new LinkedHashMap<>();
        this.directionParsers = new LinkedHashMap<>();
        this.particleDataParsers = new LinkedHashMap<>();
    }

    /**
     * Registers a parser if no parser for the normalized type exists yet.
     *
     * @param parsers    the parser map
     * @param type       the parser type
     * @param parser     the parser instance
     * @param parserKind the human-readable parser category
     * @param <P>        the parser type
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    private static <P> void registerUnique(Map<String, P> parsers, String type, P parser, String parserKind) {
        String normalizedType = EffectConfigValues.normalizeType(type);
        Objects.requireNonNull(parser, "parser");
        if (parsers.containsKey(normalizedType)) {
            throw new IllegalArgumentException(parserKind + " parser already registered for type: " + normalizedType);
        }
        parsers.put(normalizedType, parser);
    }

    /**
     * Returns the registered parser types from the given parser map.
     *
     * @param parsers the parser map
     * @return sorted set of registered parser types
     */
    private static Set<String> knownTypes(Map<String, ?> parsers) {
        return new TreeSet<>(parsers.keySet());
    }

    /**
     * Registers a shape parser for the given type.
     *
     * @param type   the parser type
     * @param parser the shape parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    /* default */ void registerShape(String type, ShapeConfigParser parser) {
        registerUnique(shapeParsers, type, parser, "Shape");
    }

    /**
     * Registers a transform parser for the given type.
     *
     * @param type   the parser type
     * @param parser the transform parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    /* default */ void registerTransform(String type, TransformConfigParser parser) {
        registerUnique(transformParsers, type, parser, "Transform");
    }

    /**
     * Registers a modifier parser for the given type.
     *
     * @param type   the parser type
     * @param parser the modifier parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    /* default */ void registerModifier(String type, ModifierConfigParser parser) {
        registerUnique(modifierParsers, type, parser, "Modifier");
    }

    /**
     * Registers a direction parser for the given type.
     *
     * @param type   the parser type
     * @param parser the direction parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    /* default */ void registerDirection(String type, DirectionConfigParser parser) {
        registerUnique(directionParsers, type, parser, "Direction");
    }

    /**
     * Registers a particle data parser for the given type.
     *
     * @param type   the parser type
     * @param parser the particle data parser
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if a parser for the normalized type already exists
     */
    /* default */ void registerParticleData(String type, ParticleDataConfigParser parser) {
        registerUnique(particleDataParsers, type, parser, "Particle data");
    }

    /**
     * Returns the shape parser for the given type.
     *
     * @param type the parser type
     * @return the shape parser, or {@code null} if no parser is registered
     */
    /* default */ ShapeConfigParser shapeParser(String type) {
        return shapeParsers.get(EffectConfigValues.normalizeType(type));
    }

    /**
     * Returns the transform parser for the given type.
     *
     * @param type the parser type
     * @return the transform parser, or {@code null} if no parser is registered
     */
    /* default */ TransformConfigParser transformParser(String type) {
        return transformParsers.get(EffectConfigValues.normalizeType(type));
    }

    /**
     * Returns the modifier parser for the given type.
     *
     * @param type the parser type
     * @return the modifier parser, or {@code null} if no parser is registered
     */
    /* default */ ModifierConfigParser modifierParser(String type) {
        return modifierParsers.get(EffectConfigValues.normalizeType(type));
    }

    /**
     * Returns the direction parser for the given type.
     *
     * @param type the parser type
     * @return the direction parser, or {@code null} if no parser is registered
     */
    /* default */ DirectionConfigParser directionParser(String type) {
        return directionParsers.get(EffectConfigValues.normalizeType(type));
    }

    /**
     * Returns the particle data parser for the given type.
     *
     * @param type the parser type
     * @return the particle data parser, or {@code null} if no parser is registered
     */
    /* default */ ParticleDataConfigParser particleDataParser(String type) {
        return particleDataParsers.get(EffectConfigValues.normalizeType(type));
    }

    /**
     * Returns all known shape parser types.
     *
     * @return sorted set of known shape parser types
     */
    /* default */ Set<String> knownShapeTypes() {
        return knownTypes(shapeParsers);
    }

    /**
     * Returns all known transform parser types.
     *
     * @return sorted set of known transform parser types
     */
    /* default */ Set<String> knownTransformTypes() {
        return knownTypes(transformParsers);
    }

    /**
     * Returns all known modifier parser types.
     *
     * @return sorted set of known modifier parser types
     */
    /* default */ Set<String> knownModifierTypes() {
        return knownTypes(modifierParsers);
    }

    /**
     * Returns all known direction parser types.
     *
     * @return sorted set of known direction parser types
     */
    /* default */ Set<String> knownDirectionTypes() {
        return knownTypes(directionParsers);
    }

    /**
     * Returns all known particle data parser types.
     *
     * @return sorted set of known particle data parser types
     */
    /* default */ Set<String> knownParticleDataTypes() {
        return knownTypes(particleDataParsers);
    }
}