package com.github.roleplaycauldron.spellbook.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EffectModifierTest {

    @Test
    void testModifierCanMutateRemoveAppendAndPreserveOrder() {
        PointBuffer points = new PointBuffer();
        points.add(1, 0, 0);
        points.add(2, 0, 0);
        points.add(3, 0, 0);

        EffectModifier modifier = (buffer, context) -> {
            buffer.set(0, 10, 0, 0);
            buffer.remove(1);
            buffer.add(40, 0, 0);
        };

        modifier.apply(points, null);

        assertEquals(3, points.size());
        assertEquals(10f, points.x(0), 1e-6f);
        assertEquals(3f, points.x(1), 1e-6f);
        assertEquals(40f, points.x(2), 1e-6f);
    }
}
