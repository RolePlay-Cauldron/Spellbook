package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.EffectBuilder;
import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.transform.TranslateTransform;
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
        config.set("particle.data.type", "custom-data");
        config.set("transforms", List.of(Map.of("type", "custom-shift")));

        EffectConfigParser parser = EffectConfigParser.defaults()
                .registerShape("custom-point", (section, context) -> (shapeContext, points) -> points.add(1, 0, 0))
                .registerTransform("custom-shift", (section, context) -> new TranslateTransform(2, 0, 0))
                .registerParticleData("custom-data", (particle, section, context) -> "payload");

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
                eq(Particle.FLAME),
                location.capture(),
                anyInt(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                eq("payload")
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

    private static MemoryConfiguration baseConfig() {
        MemoryConfiguration config = new MemoryConfiguration();
        config.set("particle.type", "FLAME");
        return config;
    }
}
