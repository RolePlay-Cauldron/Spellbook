package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D helix-shaped particle effect or construct.
 * This class defines a helix by its radius, height, number of points, number of turns,
 * and an angular speed factor for dynamic rotational behavior.
 * The helix is defined in a 3D space and provides a mechanism to sample points
 * along its geometry based on specific parameters.
 */
public final class HelixShape implements Shape {

    private final float radius;

    private final float height;

    private final int points;

    private final float turns;

    private final float angularSpeed;

    private final float travelSpeed;

    /**
     * Constructs a new HelixShape instance representing a helix-shaped effect.
     *
     * @param radius       the radius of the helix; must be greater than 0
     * @param height       the height of the helix; must be greater than or equal to 0, or -1 to use distance between origin and target
     * @param points       the number of points to generate along the helix; must be greater than 0
     * @param turns        the number of complete turns in the helix; must be greater than 0
     * @param angularSpeed the rotational speed factor applied to the helix
     * @throws IllegalArgumentException if any of the parameters do not satisfy their constraints
     */
    public HelixShape(float radius, float height, int points, float turns, float angularSpeed) {
        this(radius, height, points, turns, angularSpeed, 0f);
    }

    /**
     * Constructs a new HelixShape instance representing a helix-shaped effect with travel speed.
     *
     * @param radius       the radius of the helix; must be greater than 0
     * @param height       the height of the helix; must be greater than or equal to 0, or -1 to use distance between origin and target
     * @param points       the number of points to generate along the helix; must be greater than 0
     * @param turns        the number of complete turns in the helix; must be greater than 0
     * @param angularSpeed the rotational speed factor applied to the helix
     * @param travelSpeed  the speed at which particles travel along the helix path (percentage per step)
     * @throws IllegalArgumentException if any of the parameters do not satisfy their constraints
     */
    public HelixShape(float radius, float height, int points, float turns, float angularSpeed, float travelSpeed) {
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        if (height < 0 && height != -1) throw new IllegalArgumentException("height must be >= 0 or -1");
        if (points <= 0) throw new IllegalArgumentException("points must be > 0");
        if (turns <= 0) throw new IllegalArgumentException("turns must be > 0");

        this.radius = radius;
        this.height = height;
        this.points = points;
        this.turns = turns;
        this.angularSpeed = angularSpeed;
        this.travelSpeed = travelSpeed;
    }

    @Override
    public List<Vector3f> sample(ShapeContext context) {
        List<Vector3f> result = new ArrayList<>(points);

        float baseAngle = context.step() * angularSpeed;

        float actualHeight = height;
        if (height == -1) {
            actualHeight = (context.origin() != null && context.target() != null)
                    ? (float) context.origin().distance(context.target())
                    : 1.0f;
        }

        float travelOffset = context.step() * travelSpeed;

        for (int i = 0; i < points; i++) {
            float t;
            if (travelSpeed != 0) {
                t = ((float) i / points + travelOffset) % 1.0f;
                if (t < 0) t += 1.0f;
            } else {
                t = points == 1 ? 0f : (float) i / (points - 1);
            }

            float angle = baseAngle + t * turns * (float) (Math.PI * 2.0);
            float x = (float) Math.cos(angle) * radius;
            float y = t * actualHeight;
            float z = (float) Math.sin(angle) * radius;

            result.add(new Vector3f(x, y, z));
        }

        return result;
    }
}
