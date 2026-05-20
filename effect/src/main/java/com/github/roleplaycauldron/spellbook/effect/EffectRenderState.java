package com.github.roleplaycauldron.spellbook.effect;

import org.bukkit.Location;
import org.joml.Vector3f;

/**
 * Reusable mutable scratch state for rendering one running effect execution.
 */
public final class EffectRenderState {

    private final PointBuffer points = new PointBuffer();

    private final ShapeContext shapeContext = new ShapeContext(0, 0, null, null);

    private final Vector3f direction = new Vector3f();

    PointBuffer points() {
        return points;
    }

    ShapeContext shapeContext(int step, double timeSeconds, Location origin, Location target) {
        return shapeContext.set(step, timeSeconds, origin, target);
    }

    Vector3f direction() {
        return direction;
    }
}
