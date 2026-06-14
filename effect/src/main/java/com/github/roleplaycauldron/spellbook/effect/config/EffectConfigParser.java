package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.DirectionProvider;
import com.github.roleplaycauldron.spellbook.effect.EffectBuilder;
import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import com.github.roleplaycauldron.spellbook.effect.EffectModifier;
import com.github.roleplaycauldron.spellbook.effect.ParticleSpec;
import com.github.roleplaycauldron.spellbook.effect.emitter.StandardParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.shape.CubeShape;
import com.github.roleplaycauldron.spellbook.effect.shape.HelixShape;
import com.github.roleplaycauldron.spellbook.effect.shape.LineShape;
import com.github.roleplaycauldron.spellbook.effect.shape.MovingPointShape;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.shape.SphereShape;
import com.github.roleplaycauldron.spellbook.effect.shape.SpiralHelixShape;
import com.github.roleplaycauldron.spellbook.effect.shape.morph.MorphPointStrategies;
import com.github.roleplaycauldron.spellbook.effect.shape.morph.MorphProgress;
import com.github.roleplaycauldron.spellbook.effect.shape.morph.MorphShape;
import com.github.roleplaycauldron.spellbook.effect.transform.LookAtTransform;
import com.github.roleplaycauldron.spellbook.effect.transform.RotationTransform;
import com.github.roleplaycauldron.spellbook.effect.transform.Transform;
import com.github.roleplaycauldron.spellbook.effect.transform.TranslateTransform;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Parses Bukkit configuration sections into Spellbook effect definitions.
 * <p>
 * A parser instance owns explicit registries for shapes, transforms,
 * modifiers, direction providers, and particle data. Use {@link #defaults()}
 * to create an independent parser with Spellbook's built-in component parsers
 * registered, then add plugin-specific parsers with the registration methods.
 * <p>
 * Default configuration uses explicit {@code type} keys for polymorphic
 * components. Supported default shape types are {@code line}, {@code sphere},
 * {@code cube}, {@code helix}, {@code spiral-helix}, {@code moving-point}, and
 * {@code morph}. Supported default transform types are {@code translate},
 * {@code rotate}, and {@code look-at}. Supported standard particle fields are
 * {@code type}, {@code count}, {@code offset-x}, {@code offset-y},
 * {@code offset-z}, and {@code extra}. Advanced particle data is parsed only
 * through registered {@link ParticleDataConfigParser particle data parsers}.
 */
public final class EffectConfigParser {

    private final Map<String, ShapeConfigParser> shapeParsers;

    private final Map<String, TransformConfigParser> transformParsers;

    private final Map<String, ModifierConfigParser> modifierParsers;

    private final Map<String, DirectionConfigParser> directionParsers;

    private final Map<String, ParticleDataConfigParser> particleDataParsers;

    /**
     * Creates an empty parser with no registered component parsers.
     */
    public EffectConfigParser() {
        shapeParsers = new java.util.LinkedHashMap<>();
        transformParsers = new java.util.LinkedHashMap<>();
        modifierParsers = new java.util.LinkedHashMap<>();
        directionParsers = new java.util.LinkedHashMap<>();
        particleDataParsers = new java.util.LinkedHashMap<>();
    }

    /**
     * Creates an independent parser with Spellbook's default component parsers.
     *
     * @return parser with built-in shapes, transforms, directions, and standard particle parsing
     */
    public static EffectConfigParser defaults() {
        EffectConfigParser parser = new EffectConfigParser();
        parser.registerDefaultShapes();
        parser.registerDefaultTransforms();
        parser.registerDefaultDirections();
        return parser;
    }

    /**
     * Registers a named shape parser.
     *
     * @param type   configuration type name
     * @param parser parser to invoke for that type
     * @return this parser
     */
    public EffectConfigParser registerShape(String type, ShapeConfigParser parser) {
        shapeParsers.put(normalizeType(type), Objects.requireNonNull(parser, "parser"));
        return this;
    }

    /**
     * Registers a named transform parser.
     *
     * @param type   configuration type name
     * @param parser parser to invoke for that type
     * @return this parser
     */
    public EffectConfigParser registerTransform(String type, TransformConfigParser parser) {
        transformParsers.put(normalizeType(type), Objects.requireNonNull(parser, "parser"));
        return this;
    }

    /**
     * Registers a named modifier parser.
     *
     * @param type   configuration type name
     * @param parser parser to invoke for that type
     * @return this parser
     */
    public EffectConfigParser registerModifier(String type, ModifierConfigParser parser) {
        modifierParsers.put(normalizeType(type), Objects.requireNonNull(parser, "parser"));
        return this;
    }

    /**
     * Registers a named direction provider parser.
     *
     * @param type   configuration type name
     * @param parser parser to invoke for that type
     * @return this parser
     */
    public EffectConfigParser registerDirection(String type, DirectionConfigParser parser) {
        directionParsers.put(normalizeType(type), Objects.requireNonNull(parser, "parser"));
        return this;
    }

    /**
     * Registers a named advanced particle data parser.
     *
     * @param type   configuration data type name
     * @param parser parser to invoke for that data type
     * @return this parser
     */
    public EffectConfigParser registerParticleData(String type, ParticleDataConfigParser parser) {
        particleDataParsers.put(normalizeType(type), Objects.requireNonNull(parser, "parser"));
        return this;
    }

    /**
     * Parses an effect builder from a whole effect configuration section.
     *
     * @param section effect configuration section
     * @return configured effect builder
     * @throws EffectConfigException when the section is invalid
     */
    public EffectBuilder parseBuilder(ConfigurationSection section) {
        Objects.requireNonNull(section, "section");
        EffectBuilder builder = EffectBuilder.create();
        builder.shape(parseShape(requiredSection(section, "shape", "shape"), "shape"));
        builder.particle(parseParticle(requiredSection(section, "particle", "particle"), "particle"));

        for (IndexedSection transform : sectionList(section, "transforms", "transforms")) {
            builder.transform(parseTransform(transform.section(), transform.path()));
        }
        for (IndexedSection modifier : sectionList(section, "modifiers", "modifiers")) {
            builder.modifier(parseModifier(modifier.section(), modifier.path()));
        }

        ConfigurationSection direction = section.getConfigurationSection("direction");
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
     * @throws EffectConfigException when the section is invalid
     */
    public EffectInstance parse(ConfigurationSection section) {
        return parseBuilder(section).build();
    }

    Shape parseShape(ConfigurationSection section, String path) {
        return parseTyped(section, path, "shape", shapeParsers, parser -> parser.parse(section, new EffectConfigContext(this, path)));
    }

    Transform parseTransform(ConfigurationSection section, String path) {
        return parseTyped(section, path, "transform", transformParsers, parser -> parser.parse(section, new EffectConfigContext(this, path)));
    }

    EffectModifier parseModifier(ConfigurationSection section, String path) {
        return parseTyped(section, path, "modifier", modifierParsers, parser -> parser.parse(section, new EffectConfigContext(this, path)));
    }

    DirectionProvider parseDirection(ConfigurationSection section, String path) {
        return parseTyped(section, path, "direction", directionParsers, parser -> parser.parse(section, new EffectConfigContext(this, path)));
    }

    private <P, T> T parseTyped(
            ConfigurationSection section,
            String path,
            String component,
            Map<String, P> parsers,
            Function<P, T> invoker
    ) {
        String type = requireString(section, "type", path + ".type");
        P parser = parsers.get(normalizeType(type));
        if (parser == null) {
            throw new EffectConfigException(path + ".type", "Unknown " + component + " type '" + type + "'. Registered types: " + knownTypes(parsers));
        }
        try {
            return invoker.apply(parser);
        } catch (EffectConfigException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new EffectConfigException(fieldPathForException(section, path, exception), exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            throw new EffectConfigException(path, "Failed to parse " + component, exception);
        }
    }

    StandardParticleEmitter<Object> parseParticle(ConfigurationSection section, String path) {
        Particle particle = parseEnum(Particle.class, requireString(section, "type", path + ".type"), path + ".type");
        int count = section.getInt("count", 1);
        double offsetX = section.getDouble("offset-x", 0.0);
        double offsetY = section.getDouble("offset-y", 0.0);
        double offsetZ = section.getDouble("offset-z", 0.0);
        double extra = section.getDouble("extra", 0.0);
        Object data = null;

        ConfigurationSection dataSection = optionalSection(section, "data", path + ".data");
        if (dataSection != null) {
            String dataType = requireString(dataSection, "type", path + ".data.type");
            ParticleDataConfigParser parser = particleDataParsers.get(normalizeType(dataType));
            if (parser == null) {
                throw new EffectConfigException(path + ".data.type", "Unknown particle data type '" + dataType + "'. Registered types: " + knownTypes(particleDataParsers));
            }
            try {
                data = parser.parse(particle, dataSection, new EffectConfigContext(this, path + ".data"));
            } catch (EffectConfigException exception) {
                throw exception;
            } catch (IllegalArgumentException exception) {
                throw new EffectConfigException(path + ".data", exception.getMessage(), exception);
            } catch (RuntimeException exception) {
                throw new EffectConfigException(path + ".data", "Failed to parse particle data", exception);
            }
        }

        return new StandardParticleEmitter<>(new ParticleSpec<>(particle, count, offsetX, offsetY, offsetZ, extra, data));
    }

    private void registerDefaultShapes() {
        registerShape("line", (section, context) -> new LineShape(context.requireInt(section, "points")));
        registerShape("sphere", (section, context) -> new SphereShape(
                context.requireFloat(section, "radius"),
                context.requireInt(section, "points"),
                context.getFloat(section, "angular-speed", 0f)
        ));
        registerShape("cube", (section, context) -> new CubeShape(
                context.requireFloat(section, "size"),
                context.requireInt(section, "points-per-edge")
        ));
        registerShape("helix", (section, context) -> new HelixShape(
                context.requireInt(section, "strands"),
                context.requireInt(section, "particles-per-strand"),
                context.requireFloat(section, "radius"),
                context.requireFloat(section, "height"),
                context.requireFloat(section, "turns"),
                context.getFloat(section, "rotation-speed", 0f)
        ));
        registerShape("spiral-helix", (section, context) -> new SpiralHelixShape(
                context.requireInt(section, "strands"),
                context.requireInt(section, "particles-per-strand"),
                context.requireFloat(section, "radius"),
                context.requireFloat(section, "height"),
                context.requireFloat(section, "curve"),
                context.getFloat(section, "rotation-speed", 0f),
                section.getBoolean("reverse", false)
        ));
        registerShape("moving-point", (section, context) -> new MovingPointShape(
                context.requireFloat(section, "speed"),
                context.requireFloat(section, "spacing"),
                context.requireInt(section, "amount-points"),
                section.getBoolean("ping-pong", false)
        ));
        registerShape("morph", this::parseMorphShape);
    }

    private Shape parseMorphShape(ConfigurationSection section, EffectConfigContext context) {
        Shape source = context.parseShape(context.requireSection(section, "source"), context.path("source"));
        Shape target = context.parseShape(context.requireSection(section, "target"), context.path("target"));
        MorphShape.Builder builder = MorphShape.between(source, target);

        ConfigurationSection progress = context.optionalSection(section, "progress");
        if (progress != null) {
            builder.progress(parseMorphProgress(progress, context.path("progress")));
        }

        String strategy = section.getString("strategy", "match-index");
        builder.strategy(switch (normalizeType(strategy)) {
            case "match-index" -> MorphPointStrategies.matchIndex();
            case "resample-source-to-target" -> MorphPointStrategies.resampleSourceToTarget();
            case "resample-target-to-source" -> MorphPointStrategies.resampleTargetToSource();
            case "resample-to-max" -> MorphPointStrategies.resampleToMax();
            default -> throw new EffectConfigException(context.path("strategy"), "Unknown morph point strategy '" + strategy + "'");
        });
        return builder.build();
    }

    private MorphProgress parseMorphProgress(ConfigurationSection section, String path) {
        String type = requireString(section, "type", path + ".type");
        return switch (normalizeType(type)) {
            case "fixed" -> MorphProgress.fixed(requireFloat(section, "value", path + ".value"));
            case "over-steps" -> MorphProgress.overSteps(requireInt(section, "duration-steps", path + ".duration-steps"));
            case "after-step" -> MorphProgress.afterStep(
                    requireInt(section, "start-step", path + ".start-step"),
                    requireInt(section, "duration-steps", path + ".duration-steps")
            );
            case "over-seconds" -> MorphProgress.overSeconds(requireDouble(section, "duration-seconds", path + ".duration-seconds"));
            case "after-seconds" -> MorphProgress.afterSeconds(
                    requireDouble(section, "start-seconds", path + ".start-seconds"),
                    requireDouble(section, "duration-seconds", path + ".duration-seconds")
            );
            default -> throw new EffectConfigException(path + ".type", "Unknown morph progress type '" + type + "'");
        };
    }

    private void registerDefaultTransforms() {
        registerTransform("translate", (section, context) -> new TranslateTransform(
                context.requireFloat(section, "x"),
                context.requireFloat(section, "y"),
                context.requireFloat(section, "z")
        ));
        registerTransform("rotate", (section, context) -> new RotationTransform(
                context.getFloat(section, "yaw", 0f),
                context.getFloat(section, "pitch", 0f),
                context.getFloat(section, "roll", 0f)
        ));
        registerTransform("look-at", (section, context) -> {
            ConfigurationSection forwardAxis = context.optionalSection(section, "forward-axis");
            if (forwardAxis == null) {
                return new LookAtTransform();
            }
            EffectConfigContext axisContext = context.child("forward-axis");
            return new LookAtTransform(new Vector3f(
                    axisContext.requireFloat(forwardAxis, "x"),
                    axisContext.requireFloat(forwardAxis, "y"),
                    axisContext.requireFloat(forwardAxis, "z")
            ));
        });
    }

    private void registerDefaultDirections() {
        registerDirection("none", (section, context) -> (localX, localY, localZ, effectContext, destination) -> destination.set(0, 0, 0));
        registerDirection("toward-target", (section, context) -> (localX, localY, localZ, effectContext, destination) -> {
            if (effectContext.target() == null) {
                destination.set(0, 0, 0);
                return;
            }
            destination.set(
                    (float) (effectContext.target().getX() - effectContext.origin().getX()),
                    (float) (effectContext.target().getY() - effectContext.origin().getY()),
                    (float) (effectContext.target().getZ() - effectContext.origin().getZ())
            );
            if (destination.lengthSquared() > 1e-6f) {
                destination.normalize();
            }
        });
    }

    static ConfigurationSection requiredSection(ConfigurationSection parent, String key, String path) {
        ConfigurationSection section = optionalSection(parent, key, path);
        if (section == null) {
            throw new EffectConfigException(path, "Missing required section");
        }
        return section;
    }

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

    static List<IndexedSection> sectionList(ConfigurationSection parent, String key, String path) {
        if (!parent.contains(key)) {
            return List.of();
        }
        Object value = parent.get(key);
        if (!(value instanceof List<?> values)) {
            throw new EffectConfigException(path, "Expected list");
        }
        List<IndexedSection> sections = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            String elementPath = path + "[" + i + "]";
            Object element = values.get(i);
            if (element instanceof ConfigurationSection section) {
                sections.add(new IndexedSection(section, elementPath));
            } else if (element instanceof Map<?, ?> map) {
                sections.add(new IndexedSection(sectionFromMap(map, elementPath), elementPath));
            } else {
                throw new EffectConfigException(elementPath, "Expected configuration section");
            }
        }
        return sections;
    }

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

    static String requireString(ConfigurationSection section, String key, String path) {
        String value = section.getString(key);
        if (value == null || value.isBlank()) {
            throw new EffectConfigException(path, "Missing required string");
        }
        return value;
    }

    static int requireInt(ConfigurationSection section, String key, String path) {
        if (!section.contains(key)) {
            throw new EffectConfigException(path, "Missing required integer");
        }
        return section.getInt(key);
    }

    static float requireFloat(ConfigurationSection section, String key, String path) {
        if (!section.contains(key)) {
            throw new EffectConfigException(path, "Missing required number");
        }
        return (float) section.getDouble(key);
    }

    static double requireDouble(ConfigurationSection section, String key, String path) {
        if (!section.contains(key)) {
            throw new EffectConfigException(path, "Missing required number");
        }
        return section.getDouble(key);
    }

    static float getFloat(ConfigurationSection section, String key, float fallback) {
        return section.contains(key) ? (float) section.getDouble(key) : fallback;
    }

    static <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String path) {
        try {
            return Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException exception) {
            throw new EffectConfigException(path, "Unknown " + enumType.getSimpleName() + " value '" + value + "'", exception);
        }
    }

    static String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be blank");
        }
        return type.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static String fieldPathForException(ConfigurationSection section, String path, IllegalArgumentException exception) {
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

    private static Set<String> knownTypes(Map<String, ?> parsers) {
        return new TreeSet<>(parsers.keySet());
    }

    /**
     * A configuration section paired with its indexed path.
     *
     * @param section configuration section
     * @param path    section path
     */
    public record IndexedSection(ConfigurationSection section, String path) {
    }
}
