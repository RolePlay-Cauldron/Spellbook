package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sphere-shaped particle effect.
 * This shape samples points on the surface of a sphere.
 */
public final class SphereShape implements Shape {

    private static final float GOLDEN_ANGLE = (float) (Math.PI * (3.0 - Math.sqrt(5.0)));

    private final float radius;

    private final int points;

    private final float angularSpeed;

    /**
     * Creates a static sphere shape.
     *
     * @param radius the sphere radius; must be greater than 0
     * @param points the number of points to sample on the sphere surface; must be greater than 0
     */
    public SphereShape(float radius, int points) {
        this(radius, points, 0f);
    }

    /**
     * Creates a sphere shape with optional rotation over steps.
     *
     * @param radius       the sphere radius; must be greater than 0
     * @param points       the number of points to sample on the sphere surface; must be greater than 0
     * @param angularSpeed additional angle in radians per step for rotating the point distribution
     */
    public SphereShape(float radius, int points, float angularSpeed) {
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        if (points <= 0) throw new IllegalArgumentException("points must be > 0");

        this.radius = radius;
        this.points = points;
        this.angularSpeed = angularSpeed;
    }

    @Override
    public List<Vector3f> sample(ShapeContext context) {
        List<Vector3f> result = new ArrayList<>(points);
        float baseAngle = context.step() * angularSpeed;

        for (int i = 0; i < points; i++) {
            float t = points == 1 ? 0.5f : (float) i / (points - 1);
            float y = 1f - 2f * t;
            float radial = (float) Math.sqrt(Math.max(0f, 1f - y * y));
            float angle = i * GOLDEN_ANGLE + baseAngle;

            float x = (float) Math.cos(angle) * radial;
            float z = (float) Math.sin(angle) * radial;

            result.add(new Vector3f(x * radius, y * radius, z * radius));
        }

        return result;
    }
}
