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
 * Unit tests for the MovingPointShape class.
 */
class MovingPointShapeTest {

    @Test
    void testRejectsInvalidSpeed() {
        assertThrows(IllegalArgumentException.class, () -> new MovingPointShape(0f, 0.5f, 1, false));
    }

    @Test
    void testRejectsInvalidSpacing() {
        assertThrows(IllegalArgumentException.class, () -> new MovingPointShape(0.5f, 0f, 1, false));
    }

    @Test
    void testRejectsInvalidAmountPoints() {
        assertThrows(IllegalArgumentException.class, () -> new MovingPointShape(0.5f, 0.5f, 0, false));
    }

    @Test
    void testReturnsEmptyWhenOriginOrTargetIsMissing() {
        MovingPointShape shape = new MovingPointShape(0.5f, 0.5f, 3, false);

        List<Vector3f> noOrigin = shape.sample(new ShapeContext(0, 0.0, null, createLocation(0, 1, 0)));
        List<Vector3f> noTarget = shape.sample(new ShapeContext(0, 0.0, createLocation(0, 0, 0), null));

        assertEquals(0, noOrigin.size());
        assertEquals(0, noTarget.size());
    }

    @Test
    void testReturnsEmptyWhenOriginAndTargetAreEqual() {
        MovingPointShape shape = new MovingPointShape(0.5f, 0.5f, 3, false);
        List<Vector3f> points = shape.sample(createContext(2, 1, 1, 1, 1, 1, 1));

        assertEquals(0, points.size());
    }

    @Test
    void testMovesWithWrappedDistanceInLoopMode() {
        MovingPointShape shape = new MovingPointShape(1f, 2f, 1, false);
        List<Vector3f> points = shape.sample(createContext(6, 0, 0, 0, 0, 10, 0));

        assertEquals(1, points.size());
        assertEquals(0f, points.getFirst().x, 1e-6f);
        assertEquals(2f, points.getFirst().y, 1e-6f);
        assertEquals(0f, points.getFirst().z, 1e-6f);
    }

    @Test
    void testMovesBackAndForthInPingPongMode() {
        MovingPointShape shape = new MovingPointShape(1f, 2f, 1, true);
        Vector3f point = shape.sample(createContext(6, 0, 0, 0, 0, 10, 0)).getFirst();

        assertEquals(8f, point.y, 1e-6f);
    }

    @Test
    void testReturnsTrailPointsUpToAmountPoints() {
        MovingPointShape shape = new MovingPointShape(1f, 2f, 3, false);
        List<Vector3f> points = shape.sample(createContext(5, 0, 0, 0, 0, 10, 0));

        assertEquals(3, points.size());
        assertEquals(0f, points.get(0).y, 1e-6f);
        assertEquals(8f, points.get(1).y, 1e-6f);
        assertEquals(6f, points.get(2).y, 1e-6f);
    }

    @Test
    void testSkipsTrailPointsBeforeEnoughDistanceIsTraveled() {
        MovingPointShape shape = new MovingPointShape(1f, 2f, 3, false);
        List<Vector3f> points = shape.sample(createContext(1, 0, 0, 0, 0, 10, 0));

        assertEquals(2, points.size());
        assertEquals(2f, points.get(0).y, 1e-6f);
        assertEquals(0f, points.get(1).y, 1e-6f);
    }

    private ShapeContext createContext(int step, double x1, double y1, double z1, double x2, double y2, double z2) {
        return new ShapeContext(step, 0.0, createLocation(x1, y1, z1), createLocation(x2, y2, z2));
    }

    private Location createLocation(double x, double y, double z) {
        World world = Mockito.mock(World.class);
        return new Location(world, x, y, z);
    }
}
