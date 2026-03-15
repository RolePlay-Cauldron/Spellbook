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
 * Unit test class for verifying the behavior of the LineShape class.
 */
class LineShapeTest {

    @Test
    void testRejectsInvalidPointCount() {
        assertThrows(IllegalArgumentException.class, () -> new LineShape(0));
    }

    @Test
    void testSamplesLineEndpointsAndMiddle() {
        LineShape shape = new LineShape(3);
        List<Vector3f> points = shape.sample(createContext(0, 0, 0, 0, 0, 10, 0));

        assertEquals(3, points.size());
        assertEquals(0f, points.getFirst().x, 1e-6f);
        assertEquals(0f, points.getFirst().y, 1e-6f);
        assertEquals(0f, points.getFirst().z, 1e-6f);

        assertEquals(0f, points.get(1).x, 1e-6f);
        assertEquals(5f, points.get(1).y, 1e-6f);
        assertEquals(0f, points.get(1).z, 1e-6f);

        assertEquals(0f, points.get(2).x, 1e-6f);
        assertEquals(10f, points.get(2).y, 1e-6f);
        assertEquals(0f, points.get(2).z, 1e-6f);
    }

    @Test
    void testSinglePointReturnsStart() {
        LineShape shape = new LineShape(1);
        List<Vector3f> points = shape.sample(createContext(0, 3, 4, 5, 0, 0, 0));

        assertEquals(1, points.size());
        assertEquals(0f, points.getFirst().x, 1e-6f);
        assertEquals(0f, points.getFirst().y, 1e-6f);
        assertEquals(0f, points.getFirst().z, 1e-6f);
    }

    private ShapeContext createContext(int step, double x1, double y1, double z1, double x2, double y2, double z2) {
        World world = Mockito.mock(World.class);
        Location origin = new Location(world, x1, y1, z1);
        Location target = new Location(world, x2, y2, z2);
        return new ShapeContext(step, 0.0, origin, target);
    }
}
