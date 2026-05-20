package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import com.github.roleplaycauldron.spellbook.effect.ShapeContext;

/**
 * Represents a sphere-shaped particle effect.
 * This shape samples points on the surface of a sphere.
 */
public final class SphereShape implements Shape {

    private static final float GOLDEN_ANGLE = (float) (Math.PI * (3.0 - Math.sqrt(5.0)));

    private final float radius;

    private final int points;

    private final float angularSpeed;

    private final float[] staticPoints;

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
        this.staticPoints = angularSpeed == 0f ? buildPoints(0f) : null;
    }

    @Override
    public void sample(ShapeContext context, PointBuffer points) {
        float baseAngle = context.step() * angularSpeed;
        float[] sampled = staticPoints == null ? buildPoints(baseAngle) : staticPoints;

        points.ensureCapacity(points.size() + this.points);
        for (int i = 0; i < sampled.length; i += 3) {
            points.add(sampled[i], sampled[i + 1], sampled[i + 2]);
        }
    }

    private float[] buildPoints(float baseAngle) {
        float[] result = new float[points * 3];
        int index = 0;

        for (int i = 0; i < points; i++) {
            float t = points == 1 ? 0.5f : (float) i / (points - 1);
            float y = 1f - 2f * t;
            float radial = (float) Math.sqrt(Math.max(0f, 1f - y * y));
            float angle = i * GOLDEN_ANGLE + baseAngle;

            float x = (float) Math.cos(angle) * radial;
            float z = (float) Math.sin(angle) * radial;

            result[index++] = x * radius;
            result[index++] = y * radius;
            result[index++] = z * radius;
        }

        return result;
    }
}
