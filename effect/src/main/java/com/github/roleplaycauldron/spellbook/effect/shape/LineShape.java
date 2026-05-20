package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

/**
 * A {@code LineShape} represents a straight-line shape that generates a series of 3D points
 * along a linear interpolation from an origin to a target. The number of points generated
 * is specified during construction.
 */
public final class LineShape implements Shape {

    private final int points;

    /**
     * Constructs a {@code LineShape} object with a specified number of points,
     * representing a linear shape for generating 3D points or particles.
     *
     * @param points the number of points to generate along the line; must be greater than 0
     * @throws IllegalArgumentException if the {@code points} parameter is less than or equal to 0
     */
    public LineShape(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("points must be > 0");
        }
        this.points = points;
    }

    @Override
    public void sample(ShapeContext context, PointBuffer points) {
        if (context.origin() == null || context.target() == null) {
            return;
        }

        Vector3f origin = context.origin().toVector().toVector3f();
        Vector3f target = context.target().toVector().toVector3f();
        Vector3f direction = new Vector3f(target).sub(origin);

        points.ensureCapacity(points.size() + this.points);

        if (this.points == 1) {
            points.add(0, 0, 0);
            return;
        }

        for (int i = 0; i < this.points; i++) {
            float t = (float) i / (this.points - 1);
            points.add(direction.x * t, direction.y * t, direction.z * t);
        }
    }
}
