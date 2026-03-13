package com.github.roleplaycauldron.spellbook.effect.transform;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LookAtTransform}
 */
class LookAtTransformTest {

    @Test
    void testLookAtRotation() {
        LookAtTransform transform = new LookAtTransform();

        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, 10, 0, 0);

        EffectContext context = new EffectContext(world, origin, target, null, 0, 0, 0);
        Vector3f point = new Vector3f(0, 1, 0);
        Vector3f result = transform.apply(point, context);

        assertEquals(1.0f, result.x, 1e-6, "X should be 1");
        assertEquals(0.0f, result.y, 1e-6, "Y should be 0");
        assertEquals(0.0f, result.z, 1e-6, "Z should be 0");
    }

    @Test
    void testLookAtRotationReverse() {
        LookAtTransform transform = new LookAtTransform();

        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, -10, 0, 0);

        EffectContext context = new EffectContext(world, origin, target, null, 0, 0, 0);
        Vector3f point = new Vector3f(0, 1, 0);
        Vector3f result = transform.apply(point, context);

        assertEquals(-1.0f, result.x, 1e-6, "X should be -1");
        assertEquals(0.0f, result.y, 1e-6, "Y should be 0");
        assertEquals(0.0f, result.z, 1e-6, "Z should be 0");
    }

    @Test
    void testLookAtRotationDiagonal() {
        LookAtTransform transform = new LookAtTransform();

        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, 10, 10, 0);

        EffectContext context = new EffectContext(world, origin, target, null, 0, 0, 0);
        Vector3f point = new Vector3f(0, 1, 0);
        Vector3f result = transform.apply(point, context);

        float expected = (float) (1.0 / Math.sqrt(2));
        assertEquals(expected, result.x, 1e-6, "X should be ~0.707");
        assertEquals(expected, result.y, 1e-6, "Y should be ~0.707");
        assertEquals(0.0f, result.z, 1e-6, "Z should be 0");
    }

    @Test
    void testCustomForwardAxis() {
        LookAtTransform transform = new LookAtTransform(new Vector3f(1, 0, 0));

        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, 0, 10, 0);

        EffectContext context = new EffectContext(world, origin, target, null, 0, 0, 0);
        Vector3f point = new Vector3f(1, 0, 0);
        Vector3f result = transform.apply(point, context);

        assertEquals(0.0f, result.x, 1e-6);
        assertEquals(1.0f, result.y, 1e-6);
        assertEquals(0.0f, result.z, 1e-6);
    }
}
