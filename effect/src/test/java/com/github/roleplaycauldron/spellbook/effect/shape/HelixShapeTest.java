package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HelixShapeTest {

    @Test
    void testRejectsInvalidRadius() {
        assertThrows(IllegalArgumentException.class, () -> new HelixShape(0f, 1f, 16, 1f, 0f));
    }

    @Test
    void testRejectsInvalidHeight() {
        assertThrows(IllegalArgumentException.class, () -> new HelixShape(1f, -2f, 16, 1f, 0f));
    }

    @Test
    void testRejectsInvalidPointCount() {
        assertThrows(IllegalArgumentException.class, () -> new HelixShape(1f, 1f, 0, 1f, 0f));
    }

    @Test
    void testRejectsInvalidTurns() {
        assertThrows(IllegalArgumentException.class, () -> new HelixShape(1f, 1f, 16, 0f, 0f));
    }

    @Test
    void testSamplesExpectedPointCountAndRadius() {
        HelixShape shape = new HelixShape(2f, 4f, 32, 2f, 0f);
        List<Vector3f> points = shape.sample(createContext(0, 0, 0, 0, 0, 8, 0));

        assertEquals(32, points.size());
        for (Vector3f point : points) {
            assertEquals(2f, (float) Math.sqrt(point.x * point.x + point.z * point.z), 1e-4f);
        }
        assertEquals(0f, points.get(0).y, 1e-6f);
        assertEquals(4f, points.get(points.size() - 1).y, 1e-4f);
    }

    @Test
    void testHeightMinusOneUsesOriginTargetDistance() {
        HelixShape shape = new HelixShape(1f, -1f, 4, 1f, 0f);
        List<Vector3f> points = shape.sample(createContext(0, 0, 0, 0, 0, 10, 0));

        assertEquals(10f, points.get(points.size() - 1).y, 1e-6f);
    }

    @Test
    void testAngularSpeedDependsOnStep() {
        HelixShape shape = new HelixShape(1f, 2f, 8, 1f, 0.5f);

        Vector3f pointStep0 = shape.sample(createContext(0, 0, 0, 0, 0, 1, 0)).get(0);
        Vector3f pointStep1 = shape.sample(createContext(1, 0, 0, 0, 0, 1, 0)).get(0);

        assertNotEquals(pointStep0.x, pointStep1.x, 1e-6f);
    }

    @Test
    void testTravelSpeedShiftsPoints() {
        HelixShape shape = new HelixShape(1f, 10f, 2, 1f, 0f, 0.1f);

        List<Vector3f> points0 = shape.sample(createContext(0, 0, 0, 0, 0, 1, 0));
        List<Vector3f> points1 = shape.sample(createContext(1, 0, 0, 0, 0, 1, 0));

        assertEquals(0f, points0.get(0).y, 1e-6f);
        assertEquals(1f, points1.get(0).y, 1e-6f);
    }

    private ShapeContext createContext(int step, double x1, double y1, double z1, double x2, double y2, double z2) {
        World world = Mockito.mock(World.class);
        Location origin = new Location(world, x1, y1, z1);
        Location target = new Location(world, x2, y2, z2);
        return new ShapeContext(step, 0.0, origin, target);
    }
}
