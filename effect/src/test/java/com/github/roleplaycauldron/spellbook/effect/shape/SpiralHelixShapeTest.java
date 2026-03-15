package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for verifying the behavior and validation logic of the SpiralHelixShape class.
 */
class SpiralHelixShapeTest {

    @Test
    void testRejectsInvalidStrandCount() {
        assertThrows(IllegalArgumentException.class, () -> new SpiralHelixShape(0, 8, 1f, 2f, 1f, 0f));
    }

    @Test
    void testRejectsInvalidParticlesPerStrand() {
        assertThrows(IllegalArgumentException.class, () -> new SpiralHelixShape(1, 0, 1f, 2f, 1f, 0f));
    }

    @Test
    void testRejectsInvalidRadius() {
        assertThrows(IllegalArgumentException.class, () -> new SpiralHelixShape(1, 8, 0f, 2f, 1f, 0f));
    }

    @Test
    void testSamplesExpectedPointCountAndBounds() {
        SpiralHelixShape shape = new SpiralHelixShape(2, 10, 3f, 6f, 2f, 0f);
        List<Vector3f> points = shape.sample(createContext(0));

        assertEquals(20, points.size());

        for (Vector3f point : points) {
            float radialDistance = (float) Math.sqrt(point.x * point.x + point.z * point.z);
            assertTrue(radialDistance <= 3f + 1e-5f);
            assertTrue(point.y > 0f);
            assertTrue(point.y <= 6f + 1e-6f);
        }
    }

    @Test
    void testRotationSpeedDependsOnStep() {
        SpiralHelixShape shape = new SpiralHelixShape(2, 10, 2f, 4f, 1f, 0.25f);

        Vector3f pointStep0 = shape.sample(createContext(0)).get(0);
        Vector3f pointStep1 = shape.sample(createContext(1)).get(0);

        assertNotEquals(pointStep0.x, pointStep1.x, 1e-6f);
    }

    @Test
    void testLastPointReachesConfiguredRadiusAndHeight() {
        SpiralHelixShape shape = new SpiralHelixShape(1, 4, 2f, 8f, 1f, 0f);
        List<Vector3f> points = shape.sample(createContext(0));

        Vector3f last = points.get(points.size() - 1);

        assertEquals(8f, last.y, 1e-6f);
        assertEquals(2f, (float) Math.sqrt(last.x * last.x + last.z * last.z), 1e-6f);
    }

    private ShapeContext createContext(int step) {
        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, 0, 0, 1);
        return new ShapeContext(step, 0.0, origin, target);
    }
}
