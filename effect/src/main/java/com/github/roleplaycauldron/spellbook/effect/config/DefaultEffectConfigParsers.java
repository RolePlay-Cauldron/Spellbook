package com.github.roleplaycauldron.spellbook.effect.config;

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
import com.github.roleplaycauldron.spellbook.effect.transform.TranslateTransform;
import org.bukkit.configuration.ConfigurationSection;
import org.joml.Vector3f;

/**
 * Registers Spellbook's built-in effect configuration parsers.
 *
 * <p>This class contains the default parser registrations used by
 * {@link EffectConfigParser#defaults()}. It registers built-in shapes,
 * transforms, and direction providers. The registered parsers read Bukkit
 * {@link ConfigurationSection configuration sections} and create the matching
 * effect components.</p>
 */
final class DefaultEffectConfigParsers {

    /**
     * Prevent initialization.
     */
    private DefaultEffectConfigParsers() {
        // Empty
    }

    /**
     * Registers all built-in effect component parsers on the given parser.
     *
     * <p>This includes the default shape parsers, transform parsers, and
     * direction provider parsers.</p>
     *
     * @param parser parser to register defaults on
     * @throws NullPointerException     if the parser is {@code null}
     * @throws IllegalArgumentException if one of the default parser types is already registered
     */
    /* default */
    static void registerAll(EffectConfigParser parser) {
        registerShapes(parser);
        registerTransforms(parser);
        registerDirections(parser);
    }

    /**
     * Registers all built-in shape parsers.
     *
     * @param parser parser to register shapes on
     * @throws IllegalArgumentException if one of the default shape types is already registered
     */
    private static void registerShapes(EffectConfigParser parser) {
        parser.registerShape("line", (section, context) -> new LineShape(context.requireInt(section, "points")));
        parser.registerShape("sphere", (section, context) -> new SphereShape(
                context.requireFloat(section, "radius"),
                context.requireInt(section, "points"),
                context.getFloat(section, "angular-speed", 0f)
        ));
        parser.registerShape("cube", (section, context) -> new CubeShape(
                context.requireFloat(section, "size"),
                context.requireInt(section, "points-per-edge")
        ));
        parser.registerShape("helix", (section, context) -> new HelixShape(
                context.requireInt(section, "strands"),
                context.requireInt(section, "particles-per-strand"),
                context.requireFloat(section, "radius"),
                context.requireFloat(section, "height"),
                context.requireFloat(section, "turns"),
                context.getFloat(section, "rotation-speed", 0f)
        ));
        parser.registerShape("spiral-helix", (section, context) -> new SpiralHelixShape(
                context.requireInt(section, "strands"),
                context.requireInt(section, "particles-per-strand"),
                context.requireFloat(section, "radius"),
                context.requireFloat(section, "height"),
                context.requireFloat(section, "curve"),
                context.getFloat(section, "rotation-speed", 0f),
                context.getBoolean(section, "reverse", false)
        ));
        parser.registerShape("moving-point", (section, context) -> new MovingPointShape(
                context.requireFloat(section, "speed"),
                context.requireFloat(section, "spacing"),
                context.requireInt(section, "amount-points"),
                context.getBoolean(section, "ping-pong", false)
        ));
        parser.registerShape("morph", DefaultEffectConfigParsers::parseMorphShape);
    }

    /**
     * Parses a morph shape from a configuration section.
     *
     * <p>A morph shape requires a {@code source} shape and a {@code target}
     * shape. It may also define an optional {@code progress} section and a
     * point matching {@code strategy}. If no strategy is configured,
     * {@code match-index} is used.</p>
     *
     * @param section morph shape configuration section
     * @param context parsing context for nested values and error paths
     * @return parsed morph shape
     * @throws EffectConfigException if the morph configuration is invalid
     */
    private static Shape parseMorphShape(ConfigurationSection section, EffectConfigContext context) {
        Shape source = context.parseShape(context.requireSection(section, "source"), context.path("source"));
        Shape target = context.parseShape(context.requireSection(section, "target"), context.path("target"));
        MorphShape.Builder builder = MorphShape.between(source, target);

        ConfigurationSection progress = context.optionalSection(section, "progress");
        if (progress != null) {
            builder.progress(parseMorphProgress(progress, context.path("progress")));
        }

        String strategy = section.getString("strategy", "match-index");
        builder.strategy(switch (EffectConfigValues.normalizeType(strategy)) {
            case "match-index" -> MorphPointStrategies.matchIndex();
            case "resample-source-to-target" -> MorphPointStrategies.resampleSourceToTarget();
            case "resample-target-to-source" -> MorphPointStrategies.resampleTargetToSource();
            case "resample-to-max" -> MorphPointStrategies.resampleToMax();
            default ->
                    throw new EffectConfigException(context.path("strategy"), "Unknown morph point strategy '" + strategy + "'");
        });
        return builder.build();
    }

    /**
     * Parses a morph progress configuration.
     *
     * <p>Supported progress types are {@code fixed}, {@code over-steps},
     * {@code after-step}, {@code over-seconds}, and {@code after-seconds}.</p>
     *
     * @param section morph progress configuration section
     * @param path    configuration path used for error reporting
     * @return parsed morph progress
     * @throws EffectConfigException if the progress type or required fields are invalid
     */
    private static MorphProgress parseMorphProgress(ConfigurationSection section, String path) {
        String type = EffectConfigValues.requireString(section, "type", path + ".type");
        return switch (EffectConfigValues.normalizeType(type)) {
            case "fixed" -> MorphProgress.fixed(EffectConfigValues.requireFloat(section, "value", path + ".value"));
            case "over-steps" ->
                    MorphProgress.overSteps(EffectConfigValues.requireInt(section, "duration-steps", path + ".duration-steps"));
            case "after-step" -> MorphProgress.afterStep(
                    EffectConfigValues.requireInt(section, "start-step", path + ".start-step"),
                    EffectConfigValues.requireInt(section, "duration-steps", path + ".duration-steps")
            );
            case "over-seconds" ->
                    MorphProgress.overSeconds(EffectConfigValues.requireDouble(section, "duration-seconds", path + ".duration-seconds"));
            case "after-seconds" -> MorphProgress.afterSeconds(
                    EffectConfigValues.requireDouble(section, "start-seconds", path + ".start-seconds"),
                    EffectConfigValues.requireDouble(section, "duration-seconds", path + ".duration-seconds")
            );
            default -> throw new EffectConfigException(path + ".type", "Unknown morph progress type '" + type + "'");
        };
    }

    /**
     * Registers all built-in transform parsers.
     *
     * @param parser parser to register transforms on
     * @throws IllegalArgumentException if one of the default transform types is already registered
     */
    private static void registerTransforms(EffectConfigParser parser) {
        parser.registerTransform("translate", (section, context) -> new TranslateTransform(
                context.requireFloat(section, "x"),
                context.requireFloat(section, "y"),
                context.requireFloat(section, "z")
        ));
        parser.registerTransform("rotate", (section, context) -> new RotationTransform(
                context.getFloat(section, "yaw", 0f),
                context.getFloat(section, "pitch", 0f),
                context.getFloat(section, "roll", 0f)
        ));
        parser.registerTransform("look-at", (section, context) -> {
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

    /**
     * Registers all built-in direction provider parsers.
     *
     * <p>The built-in direction providers are {@code none} and
     * {@code toward-target}.</p>
     *
     * @param parser parser to register directions on
     * @throws IllegalArgumentException if one of the default direction types is already registered
     */
    private static void registerDirections(EffectConfigParser parser) {
        parser.registerDirection("none", (section, context) -> (localX, localY, localZ, effectContext, destination) -> destination.set(0, 0, 0));
        parser.registerDirection("toward-target", (section, context) -> (localX, localY, localZ, effectContext, destination) -> {
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
}
