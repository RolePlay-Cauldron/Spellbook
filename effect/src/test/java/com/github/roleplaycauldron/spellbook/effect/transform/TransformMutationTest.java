package com.github.roleplaycauldron.spellbook.effect.transform;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformMutationTest {

    @Test
    void testTranslateTransformMutatesPointInPlace() {
        PointBuffer points = new PointBuffer();
        points.add(1, 2, 3);

        new TranslateTransform(2, 3, 4).apply(points, 0, context());

        assertEquals(3f, points.x(0), 1e-6f);
        assertEquals(5f, points.y(0), 1e-6f);
        assertEquals(7f, points.z(0), 1e-6f);
    }

    @Test
    void testRotationTransformMutatesPointInPlace() {
        PointBuffer points = new PointBuffer();
        points.add(0, 0, 1);

        Transform.PreparedTransform prepared = new RotationTransform(90, 0, 0).prepare(context());
        prepared.apply(points, 0);

        assertEquals(1f, points.x(0), 1e-5f);
        assertEquals(0f, points.y(0), 1e-5f);
        assertEquals(0f, points.z(0), 1e-5f);
    }

    private EffectContext context() {
        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 0, 0, 0);
        Location target = new Location(world, 0, 0, 1);
        return new EffectContext(world, origin, target, null, 0, 0, 0);
    }
}
