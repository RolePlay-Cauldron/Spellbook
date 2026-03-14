package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CubeShapeTest {

    @Test
    void testRejectsInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> new CubeShape(0f, 2));
    }

    @Test
    void testRejectsInvalidPointsPerEdge() {
        assertThrows(IllegalArgumentException.class, () -> new CubeShape(2f, 1));
    }

    @Test
    void testSamplesExpectedPointCountAndBounds() {
        CubeShape shape = new CubeShape(2f, 3);
        List<Vector3f> points = shape.sample(createContext(0));

        assertEquals(36, points.size());

        for (Vector3f point : points) {
            assertTrue(Math.abs(point.x) <= 1f + 1e-6f);
            assertTrue(Math.abs(point.y) <= 1f + 1e-6f);
            assertTrue(Math.abs(point.z) <= 1f + 1e-6f);

            int fixedAxes = 0;
            if (Math.abs(Math.abs(point.x) - 1f) < 1e-6f) fixedAxes++;
            if (Math.abs(Math.abs(point.y) - 1f) < 1e-6f) fixedAxes++;
            if (Math.abs(Math.abs(point.z) - 1f) < 1e-6f) fixedAxes++;

            assertTrue(fixedAxes >= 2);
        }
    }

    @Test
    void testContainsCubeCorners() {
        CubeShape shape = new CubeShape(2f, 2);
        List<Vector3f> points = shape.sample(createContext(0));

        assertTrue(containsPoint(points, 1f, 1f, 1f));
        assertTrue(containsPoint(points, -1f, -1f, -1f));
    }

    private boolean containsPoint(List<Vector3f> points, float x, float y, float z) {
        for (Vector3f point : points) {
            if (Math.abs(point.x - x) < 1e-6f
                    && Math.abs(point.y - y) < 1e-6f
                    && Math.abs(point.z - z) < 1e-6f) {
                return true;
            }
        }
        return false;
    }

    private ShapeContext createContext(int step) {
        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, 0, 0, 1);
        return new ShapeContext(step, 0.0, origin, target);
    }
}
