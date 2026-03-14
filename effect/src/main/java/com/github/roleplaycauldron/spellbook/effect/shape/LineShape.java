package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a straight line-shaped particle effect or construct.
 * This class defines a line segment in 3D space, starting at (0,0,0) and extending along the Y-axis.
 * It provides a mechanism to sample points along the line based on the number of points specified.
 */
public final class LineShape implements Shape {

    private final int points;

    private final float travelSpeed;

    /**
     * Creates a new LineShape
     *
     * @param points the number of points to sample along the line
     */
    public LineShape(int points) {
        this(points, 0f);
    }

    /**
     * Creates a new LineShape with travel speed
     *
     * @param points      the number of points to sample along the line
     * @param travelSpeed the speed at which particles travel along the line (percentage per step)
     */
    public LineShape(int points, float travelSpeed) {
        if (points <= 0) throw new IllegalArgumentException("points must be > 0");

        this.points = points;
        this.travelSpeed = travelSpeed;
    }

    @Override
    public List<Vector3f> sample(ShapeContext context) {
        float length = 1.0f;
        if (context.origin() != null && context.target() != null) {
            length = (float) context.origin().distance(context.target());
        }

        List<Vector3f> result = new ArrayList<>(points);

        float travelOffset = context.step() * travelSpeed;

        for (int i = 0; i < points; i++) {
            float t;
            if (travelSpeed != 0) {
                t = ((float) i / points + travelOffset) % 1.0f;
                if (t < 0) t += 1.0f;
            } else {
                t = points == 1 ? 0f : (float) i / (points - 1);
            }
            result.add(new Vector3f(0, t * length, 0));
        }

        return result;
    }
}
