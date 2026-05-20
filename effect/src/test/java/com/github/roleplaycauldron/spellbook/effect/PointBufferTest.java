package com.github.roleplaycauldron.spellbook.effect;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointBufferTest {

    @Test
    void testAddReadMutateRemoveAndClear() {
        PointBuffer buffer = new PointBuffer(1);

        buffer.add(1, 2, 3);
        buffer.add(4, 5, 6);
        buffer.translate(0, 1, -1, 2);
        buffer.setY(1, 9);

        assertEquals(2, buffer.size());
        assertEquals(2f, buffer.x(0), 1e-6f);
        assertEquals(1f, buffer.y(0), 1e-6f);
        assertEquals(5f, buffer.z(0), 1e-6f);
        assertEquals(9f, buffer.y(1), 1e-6f);

        buffer.remove(0);

        assertEquals(1, buffer.size());
        assertEquals(4f, buffer.x(0), 1e-6f);
        assertEquals(9f, buffer.y(0), 1e-6f);
        assertEquals(6f, buffer.z(0), 1e-6f);

        buffer.clear();

        assertTrue(buffer.isEmpty());
    }

    @Test
    void testSwapAndIterationPreserveCurrentValues() {
        PointBuffer buffer = new PointBuffer();
        buffer.add(1, 0, 0);
        buffer.add(2, 0, 0);
        buffer.swap(0, 1);

        List<Float> xs = new ArrayList<>();
        buffer.forEach((index, x, y, z) -> xs.add(x));

        assertEquals(List.of(2f, 1f), xs);
    }

    @Test
    void testRemoveUnorderedMovesLastPointIntoRemovedSlot() {
        PointBuffer buffer = new PointBuffer();
        buffer.add(1, 2, 3);
        buffer.add(4, 5, 6);
        buffer.add(7, 8, 9);

        buffer.removeUnordered(0);

        assertEquals(2, buffer.size());
        assertEquals(7f, buffer.x(0), 1e-6f);
        assertEquals(8f, buffer.y(0), 1e-6f);
        assertEquals(9f, buffer.z(0), 1e-6f);
        assertEquals(4f, buffer.x(1), 1e-6f);
        assertEquals(5f, buffer.y(1), 1e-6f);
        assertEquals(6f, buffer.z(1), 1e-6f);
    }

    @Test
    void testRemoveUnorderedCanRemoveLastPoint() {
        PointBuffer buffer = new PointBuffer();
        buffer.add(1, 2, 3);
        buffer.add(4, 5, 6);

        buffer.removeUnordered(1);

        assertEquals(1, buffer.size());
        assertEquals(1f, buffer.x(0), 1e-6f);
        assertEquals(2f, buffer.y(0), 1e-6f);
        assertEquals(3f, buffer.z(0), 1e-6f);
    }

    @Test
    void testVectorSnapshotIsDefensive() {
        PointBuffer buffer = new PointBuffer();
        buffer.add(1, 2, 3);

        List<Vector3f> snapshot = buffer.toVectorList();
        snapshot.getFirst().set(9, 9, 9);

        assertEquals(1f, buffer.x(0), 1e-6f);
        assertEquals(2f, buffer.y(0), 1e-6f);
        assertEquals(3f, buffer.z(0), 1e-6f);
    }
}
