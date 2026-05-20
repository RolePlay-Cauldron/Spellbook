package com.github.roleplaycauldron.spellbook.effect;

import org.bukkit.Location;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable mutable scratch state for rendering one running effect execution.
 */
public final class EffectRenderState {

    private final PointBuffer points = new PointBuffer();

    private final List<PointBuffer> scratchBuffers = new ArrayList<>();

    private final ShapeContext shapeContext = new ShapeContext(0, 0, null, null, this::scratchBuffer);

    private final Vector3f direction = new Vector3f();

    PointBuffer points() {
        return points;
    }

    ShapeContext shapeContext(int step, double timeSeconds, Location origin, Location target) {
        return shapeContext.set(step, timeSeconds, origin, target).resetScratchScope();
    }

    Vector3f direction() {
        return direction;
    }

    void clearScratchBuffers() {
        for (PointBuffer scratchBuffer : scratchBuffers) {
            scratchBuffer.clear();
        }
    }

    private PointBuffer scratchBuffer(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        while (scratchBuffers.size() <= index) {
            scratchBuffers.add(new PointBuffer());
        }
        return scratchBuffers.get(index);
    }
}
