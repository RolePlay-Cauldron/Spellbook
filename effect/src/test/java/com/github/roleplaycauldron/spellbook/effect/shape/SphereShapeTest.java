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

class SphereShapeTest {

    @Test
    void testRejectsInvalidRadius() {
        assertThrows(IllegalArgumentException.class, () -> new SphereShape(0f, 16));
    }

    @Test
    void testRejectsInvalidPointCount() {
        assertThrows(IllegalArgumentException.class, () -> new SphereShape(1f, 0));
    }

    @Test
    void testSamplesExpectedPointCountOnRadius() {
        SphereShape shape = new SphereShape(2f, 64);
        ShapeContext context = createContext(0);

        List<Vector3f> points = shape.sample(context);
        assertEquals(64, points.size());

        for (Vector3f point : points) {
            assertEquals(2f, point.length(), 1e-4f);
        }
    }

    @Test
    void testAngularSpeedDependsOnStep() {
        SphereShape shape = new SphereShape(1f, 64, 0.25f);

        Vector3f pointStep0 = shape.sample(createContext(0)).get(1);
        Vector3f pointStep1 = shape.sample(createContext(1)).get(1);

        assertNotEquals(pointStep0.x, pointStep1.x, 1e-6f);
    }

    private ShapeContext createContext(int step) {
        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, 0, 0, 1);
        return new ShapeContext(step, 0.0, origin, target);
    }
}
