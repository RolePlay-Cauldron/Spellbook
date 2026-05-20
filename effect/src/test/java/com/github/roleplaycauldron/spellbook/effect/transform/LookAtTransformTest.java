package com.github.roleplaycauldron.spellbook.effect.transform;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
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
        Vector3f result = apply(transform, context, 0, 1, 0);

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
        Vector3f result = apply(transform, context, 0, 1, 0);

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
        Vector3f result = apply(transform, context, 0, 1, 0);

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
        Vector3f result = apply(transform, context, 1, 0, 0);

        assertEquals(0.0f, result.x, 1e-6);
        assertEquals(1.0f, result.y, 1e-6);
        assertEquals(0.0f, result.z, 1e-6);
    }

    @Test
    void testPreparedTransformCanApplyMultiplePoints() {
        LookAtTransform transform = new LookAtTransform();

        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, 10, 0, 0);

        EffectContext context = new EffectContext(world, origin, target, null, 0, 0, 0);
        PointBuffer points = new PointBuffer();
        points.add(0, 1, 0);
        points.add(0, 2, 0);

        Transform.PreparedTransform prepared = transform.prepare(context);
        prepared.apply(points, 0);
        prepared.apply(points, 1);

        assertEquals(1.0f, points.x(0), 1e-6);
        assertEquals(2.0f, points.x(1), 1e-6);
        assertEquals(0.0f, points.y(0), 1e-6);
        assertEquals(0.0f, points.y(1), 1e-6);
    }

    private Vector3f apply(Transform transform, EffectContext context, float x, float y, float z) {
        PointBuffer points = new PointBuffer();
        points.add(x, y, z);
        transform.prepare(context).apply(points, 0);
        return points.get(0, new Vector3f());
    }
}
