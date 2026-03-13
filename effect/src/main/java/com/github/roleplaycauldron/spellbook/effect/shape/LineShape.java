package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a straight line-shaped particle effect or construct.
 * This class defines a line segment between two points in 3D space.
 * It provides a mechanism to sample points along the line based on the number of points specified.
 */
public final class LineShape implements Shape {

    private final Vector3f start;

    private final Vector3f end;

    private final int points;

    /**
     * Creates a new LineShape
     *
     * @param start  the starting point of the line
     * @param end    the ending point of the line
     * @param points the number of points to sample along the line
     */
    public LineShape(Vector3f start, Vector3f end, int points) {
        if (start == null) throw new IllegalArgumentException("start must not be null");
        if (end == null) throw new IllegalArgumentException("end must not be null");
        if (points <= 0) throw new IllegalArgumentException("points must be > 0");

        this.start = new Vector3f(start);
        this.end = new Vector3f(end);
        this.points = points;
    }

    @Override
    public List<Vector3f> sample(ShapeContext context) {
        List<Vector3f> result = new ArrayList<>(points);

        for (int i = 0; i < points; i++) {
            float t = points == 1 ? 0f : (float) i / (points - 1);

            float x = start.x + (end.x - start.x) * t;
            float y = start.y + (end.y - start.y) * t;
            float z = start.z + (end.z - start.z) * t;

            result.add(new Vector3f(x, y, z));
        }

        return result;
    }
}