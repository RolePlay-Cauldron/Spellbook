package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

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
    public List<Vector3f> sample(ShapeContext context) {
        if (context.origin() == null || context.target() == null) {
            return new ArrayList<>();
        }

        Vector3f origin = context.origin().toVector().toVector3f();
        Vector3f target = context.target().toVector().toVector3f();
        Vector3f direction = new Vector3f(target).sub(origin);

        List<Vector3f> result = new ArrayList<>(points);

        if (points == 1) {
            result.add(new Vector3f(0, 0, 0));
            return result;
        }

        for (int i = 0; i < points; i++) {
            float t = (float) i / (points - 1);
            result.add(new Vector3f(direction).mul(t));
        }

        return result;
    }
}