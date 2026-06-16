package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.EffectBuilder;
import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.transform.TranslateTransform;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

class EffectConfigParserTest {

    @Test
    void parseBuilderAndInstanceFromValidConfiguration() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "sphere");
        config.set("shape.radius", 1.5);
        config.set("shape.points", 8);

        EffectConfigParser parser = EffectConfigParser.defaults();

        EffectBuilder builder = parser.parseBuilder(config);
        EffectInstance instance = parser.parse(config);

        assertNotNull(builder);
        assertNotNull(instance);
    }

    @Test
    void parsesBuiltInShapeTransformDirectionAndStandardParticle() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1);
        config.set("particle.count", 3);
        config.set("particle.offset-x", 0.1);
        config.set("particle.offset-y", 0.2);
        config.set("particle.offset-z", 0.3);
        config.set("particle.extra", 0.4);
        config.set("transforms", List.of(Map.of(
                "type", "translate",
                "x", 2.0,
                "y", 3.0,
                "z", 4.0
        )));
        config.set("direction.type", "toward-target");

        Player viewer = Mockito.mock(Player.class);
        World world = Mockito.mock(World.class);
        EffectInstance effect = EffectConfigParser.defaults().parse(config);
        effect.render(new EffectContext(
                world,
                new Location(world, 10, 20, 30),
                new Location(world, 10, 21, 30),
                List.of(viewer),
                0,
                0,
                0
        ));

        ArgumentCaptor<Location> location = ArgumentCaptor.forClass(Location.class);
        verify(viewer).spawnParticle(
                eq(Particle.FLAME),
                location.capture(),
                eq(3),
                eq(0.1),
                eq(0.2),
                eq(0.3),
                eq(0.4),
                eq(null)
        );
        assertTrue(Math.abs(location.getValue().getX() - 12.0) < 1e-6);
        assertTrue(Math.abs(location.getValue().getY() - 23.0) < 1e-6);
        assertTrue(Math.abs(location.getValue().getZ() - 34.0) < 1e-6);
    }

    @Test
    void parsesMorphShapeRecursively() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "morph");
        config.set("shape.source.type", "sphere");
        config.set("shape.source.radius", 1.0);
        config.set("shape.source.points", 4);
        config.set("shape.target.type", "cube");
        config.set("shape.target.size", 2.0);
        config.set("shape.target.points-per-edge", 2);
        config.set("shape.progress.type", "over-steps");
        config.set("shape.progress.duration-steps", 10);
        config.set("shape.strategy", "resample-to-max");

        assertDoesNotThrow(() -> EffectConfigParser.defaults().parse(config));
    }

    @Test
    void customRegistryParsersCanProvideComponentsAndParticleData() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "custom-point");
        config.set("particle.type", "DUST");
        config.set("particle.data.type", "custom-data");
        config.set("transforms", List.of(Map.of("type", "custom-shift")));
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0f);

        EffectConfigParser parser = EffectConfigParser.defaults()
                .registerShape("custom-point", (section, context) -> (shapeContext, points) -> points.add(1, 0, 0))
                .registerTransform("custom-shift", (section, context) -> new TranslateTransform(2, 0, 0))
                .registerParticleData("custom-data", (particle, section, context) -> dustOptions);

        Player viewer = Mockito.mock(Player.class);
        World world = Mockito.mock(World.class);
        parser.parse(config).render(new EffectContext(
                world,
                new Location(world, 5, 0, 0),
                null,
                List.of(viewer),
                0,
                0,
                0
        ));

        ArgumentCaptor<Location> location = ArgumentCaptor.forClass(Location.class);
        verify(viewer).spawnParticle(
                eq(Particle.DUST),
                location.capture(),
                anyInt(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                eq(dustOptions)
        );
        assertTrue(Math.abs(location.getValue().getX() - 8.0) < 1e-6);
    }

    @Test
    void registeredModifierParserCanModifyPoints() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "custom-point");
        config.set("modifiers", List.of(Map.of("type", "duplicate")));

        EffectConfigParser parser = EffectConfigParser.defaults()
                .registerShape("custom-point", (section, context) -> (shapeContext, points) -> points.add(0, 0, 0))
                .registerModifier("duplicate", (section, context) -> (points, effectContext) -> points.add(1, 0, 0));

        Player viewer = Mockito.mock(Player.class);
        World world = Mockito.mock(World.class);
        parser.parse(config).render(new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(viewer), 0, 0, 0));

        verify(viewer, atLeastOnce()).spawnParticle(eq(Particle.FLAME), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(null));
        verify(viewer, Mockito.times(2)).spawnParticle(eq(Particle.FLAME), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(null));
    }

    @Test
    void missingRequiredSectionsIncludePath() {
        MemoryConfiguration config = new MemoryConfiguration();
        config.set("particle.type", "FLAME");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("shape", exception.path());
        assertEquals("Missing required section", exception.detail());
        assertTrue(exception.getMessage().contains("shape"));
    }

    @Test
    void missingTypeFieldsIncludePath() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.radius", 1.0);
        config.set("shape.points", 8);

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("shape.type", exception.path());
        assertEquals("Missing required string", exception.detail());
        assertTrue(exception.getMessage().contains("shape.type"));
    }

    @Test
    void unknownTypesIncludeKnownTypes() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "ring");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertTrue(exception.getMessage().contains("Unknown shape type 'ring'"));
        assertTrue(exception.getMessage().contains("sphere"));
    }

    @Test
    void invalidValuesIncludeFieldPath() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "sphere");
        config.set("shape.radius", 0.0);
        config.set("shape.points", 8);

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertTrue(exception.getMessage().contains("shape.radius"));
        assertTrue(exception.getMessage().contains("radius must be > 0"));
    }

    @Test
    void nestedMorphErrorsIncludeNestedPath() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "morph");
        config.set("shape.source.type", "sphere");
        config.set("shape.source.radius", 1.0);
        config.set("shape.source.points", 4);
        config.set("shape.target.type", "sphere");
        config.set("shape.target.radius", 1.0);

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertTrue(exception.getMessage().contains("shape.target.points"));
    }

    @Test
    void unregisteredParticleDataFailsWithPath() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1);
        config.set("particle.data.type", "dust");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertTrue(exception.getMessage().contains("particle.data.type"));
        assertTrue(exception.getMessage().contains("dust"));
    }

    @Test
    void contextHelpersExposeNestedParsingAndStructuredErrors() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "wrapped");
        config.set("shape.child.type", "point");
        config.set("shape.transform.type", "translate");
        config.set("shape.transform.x", 2.0);
        config.set("shape.transform.y", 3.0);
        config.set("shape.transform.z", 4.0);

        EffectConfigParser parser = EffectConfigParser.defaults()
                .registerShape("point", (section, context) -> (shapeContext, points) -> points.add(0, 0, 0))
                .registerShape("wrapped", (section, context) -> {
                    Shape child = context.parseShape(context.requireSection(section, "child"), context.path("child"));
                    var transformContext = context.child("transform");
                    var transform = transformContext.parseTransform(context.requireSection(section, "transform"));
                    return (shapeContext, points) -> {
                        child.sample(shapeContext, points);
                        for (int index = 0; index < points.size(); index++) {
                            transform.apply(points, index, null);
                        }
                    };
                });

        Player viewer = Mockito.mock(Player.class);
        World world = Mockito.mock(World.class);
        parser.parse(config).render(new EffectContext(world, new Location(world, 5, 0, 0), null, List.of(viewer), 0, 0, 0));

        ArgumentCaptor<Location> location = ArgumentCaptor.forClass(Location.class);
        verify(viewer).spawnParticle(
                eq(Particle.FLAME),
                location.capture(),
                anyInt(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                eq(null)
        );
        assertTrue(Math.abs(location.getValue().getX() - 7.0) < 1e-6);
        assertTrue(Math.abs(location.getValue().getY() - 3.0) < 1e-6);
        assertTrue(Math.abs(location.getValue().getZ() - 4.0) < 1e-6);
    }

    @Test
    void contextRequiredFieldsReportChildPaths() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "custom-required");

        EffectConfigParser parser = EffectConfigParser.defaults()
                .registerShape("custom-required", (section, context) -> {
                    context.requireInt(section, "points");
                    return (shapeContext, points) -> points.add(0, 0, 0);
                });

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> parser.parse(config));

        assertEquals("shape.points", exception.path());
        assertEquals("Missing required integer", exception.detail());
    }

    @Test
    void requiredIntRejectsStringValues() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", "many");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("shape.points", exception.path());
        assertEquals("Expected integer, got String", exception.detail());
    }

    @Test
    void requiredIntRejectsDecimalValues() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1.5);

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("shape.points", exception.path());
        assertEquals("Expected integer, got 1.5", exception.detail());
    }

    @Test
    void requiredFloatRejectsStringValues() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "sphere");
        config.set("shape.radius", "wide");
        config.set("shape.points", 8);

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("shape.radius", exception.path());
        assertEquals("Expected number, got String", exception.detail());
    }

    @Test
    void optionalFloatRejectsStringValuesWhenPresent() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "sphere");
        config.set("shape.radius", 1.0);
        config.set("shape.points", 8);
        config.set("shape.angular-speed", "fast");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("shape.angular-speed", exception.path());
        assertEquals("Expected number, got String", exception.detail());
    }

    @Test
    void particleCountRejectsStringValues() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1);
        config.set("particle.count", "three");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("particle.count", exception.path());
        assertEquals("Expected integer, got String", exception.detail());
    }

    @Test
    void particleCountRejectsDecimalValues() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1);
        config.set("particle.count", 1.5);

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("particle.count", exception.path());
        assertEquals("Expected integer, got 1.5", exception.detail());
    }

    @Test
    void particleOffsetsRejectStringValues() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1);
        config.set("particle.offset-x", "left");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("particle.offset-x", exception.path());
        assertEquals("Expected number, got String", exception.detail());
    }

    @Test
    void booleanFieldsRejectNonBooleanValues() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "moving-point");
        config.set("shape.speed", 1.0);
        config.set("shape.spacing", 1.0);
        config.set("shape.amount-points", 3);
        config.set("shape.ping-pong", "yes");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("shape.ping-pong", exception.path());
        assertEquals("Expected boolean, got String", exception.detail());
    }

    @Test
    void invalidDirectionValueThrowsInsteadOfBeingIgnored() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1);
        config.set("direction", "toward-target");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("direction", exception.path());
        assertEquals("Expected configuration section", exception.detail());
    }

    @Test
    void duplicateRegistryRegistrationThrows() {
        EffectConfigParser parser = EffectConfigParser.defaults();

        IllegalArgumentException shapeException = assertThrows(IllegalArgumentException.class,
                () -> parser.registerShape("SPHERE", (section, context) -> (shapeContext, points) -> {
                }));
        IllegalArgumentException transformException = assertThrows(IllegalArgumentException.class,
                () -> parser.registerTransform("translate", (section, context) -> new TranslateTransform(0, 0, 0)));
        IllegalArgumentException directionException = assertThrows(IllegalArgumentException.class,
                () -> parser.registerDirection("toward_target", (section, context) -> (localX, localY, localZ, effectContext, destination) -> {
                }));

        parser.registerModifier("custom", (section, context) -> (points, effectContext) -> {
        });
        IllegalArgumentException modifierException = assertThrows(IllegalArgumentException.class,
                () -> parser.registerModifier("CUSTOM", (section, context) -> (points, effectContext) -> {
                }));

        parser.registerParticleData("custom-data", (particle, section, context) -> null);
        IllegalArgumentException particleDataException = assertThrows(IllegalArgumentException.class,
                () -> parser.registerParticleData("custom_data", (particle, section, context) -> null));

        assertEquals("Shape parser already registered for type: sphere", shapeException.getMessage());
        assertEquals("Transform parser already registered for type: translate", transformException.getMessage());
        assertEquals("Direction parser already registered for type: toward-target", directionException.getMessage());
        assertEquals("Modifier parser already registered for type: custom", modifierException.getMessage());
        assertEquals("Particle data parser already registered for type: custom-data", particleDataException.getMessage());
    }

    @Test
    void incompatibleParticleDataThrowsDuringParsing() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1);
        config.set("particle.type", "DUST");
        config.set("particle.data.type", "bad-data");

        EffectConfigParser parser = EffectConfigParser.defaults()
                .registerParticleData("bad-data", (particle, section, context) -> "payload");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> parser.parse(config));

        assertEquals("particle.data", exception.path());
        assertEquals("Expected particle data type DustOptions, got String", exception.detail());
    }

    @Test
    void particleRequiringDataFailsWhenDataIsAbsent() {
        MemoryConfiguration config = baseConfig();
        config.set("shape.type", "line");
        config.set("shape.points", 1);
        config.set("particle.type", "DUST");

        EffectConfigException exception = assertThrows(EffectConfigException.class, () -> EffectConfigParser.defaults().parse(config));

        assertEquals("particle.data", exception.path());
        assertEquals("Particle DUST requires data of type DustOptions", exception.detail());
    }

    private static MemoryConfiguration baseConfig() {
        MemoryConfiguration config = new MemoryConfiguration();
        config.set("particle.type", "FLAME");
        return config;
    }
}
