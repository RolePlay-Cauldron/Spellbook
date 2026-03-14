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

class MovingPointShapeTest {

    @Test
    void testRejectsInvalidSpeed() {
        assertThrows(IllegalArgumentException.class, () -> new MovingPointShape(0f, false));
    }

    @Test
    void testReturnsEmptyWhenOriginOrTargetIsMissing() {
        MovingPointShape shape = new MovingPointShape(0.5f, false);

        List<Vector3f> noOrigin = shape.sample(new ShapeContext(0, 0.0, null, createLocation(0, 1, 0)));
        List<Vector3f> noTarget = shape.sample(new ShapeContext(0, 0.0, createLocation(0, 0, 0), null));

        assertEquals(0, noOrigin.size());
        assertEquals(0, noTarget.size());
    }

    @Test
    void testMovesWithWrappedProgressInLoopMode() {
        MovingPointShape shape = new MovingPointShape(0.4f, false);
        List<Vector3f> points = shape.sample(createContext(3, 0, 0, 0, 0, 10, 0));

        assertEquals(1, points.size());
        assertEquals(0f, points.get(0).x, 1e-6f);
        assertEquals(2f, points.get(0).y, 1e-6f);
        assertEquals(0f, points.get(0).z, 1e-6f);
    }

    @Test
    void testMovesBackAndForthInPingPongMode() {
        MovingPointShape shape = new MovingPointShape(0.5f, true);

        Vector3f pointAtStep1 = shape.sample(createContext(1, 0, 0, 0, 0, 10, 0)).get(0);
        Vector3f pointAtStep3 = shape.sample(createContext(3, 0, 0, 0, 0, 10, 0)).get(0);

        assertEquals(5f, pointAtStep1.y, 1e-6f);
        assertEquals(5f, pointAtStep3.y, 1e-6f);
    }

    private ShapeContext createContext(int step, double x1, double y1, double z1, double x2, double y2, double z2) {
        return new ShapeContext(step, 0.0, createLocation(x1, y1, z1), createLocation(x2, y2, z2));
    }

    private Location createLocation(double x, double y, double z) {
        World world = Mockito.mock(World.class);
        return new Location(world, x, y, z);
    }
}
