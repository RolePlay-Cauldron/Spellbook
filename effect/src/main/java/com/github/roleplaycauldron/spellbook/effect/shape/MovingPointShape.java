package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shape that generates a series of points moving along a straight
 * line between an origin and a target. The points move at a specified speed and
 * with constant spacing, optionally bouncing back and forth along the line
 * in a "ping-pong" motion.
 */
public final class MovingPointShape implements Shape {

    private final float speed;

    private final float spacing;

    private final int amountPoints;

    private final boolean pingPong;

    /**
     * Constructs a MovingPointShape with the specified parameters.
     *
     * @param speed        the speed at which the points move along the shape, must be greater than 0
     * @param spacing      the spacing between each point along the shape, must be greater than 0
     * @param amountPoints the number of points to generate along the shape, must be greater than 0
     * @param pingPong     whether the points should bounce back and forth along the line in a "ping-pong" motion
     * @throws IllegalArgumentException if speed, spacing, or amountPoints is less than or equal to 0
     */
    public MovingPointShape(float speed, float spacing, int amountPoints, boolean pingPong) {
        if (speed <= 0f) {
            throw new IllegalArgumentException("speed must be > 0");
        }
        if (spacing <= 0f) {
            throw new IllegalArgumentException("spacing must be > 0");
        }
        if (amountPoints <= 0) {
            throw new IllegalArgumentException("amountPoints must be > 0");
        }

        this.speed = speed;
        this.spacing = spacing;
        this.amountPoints = amountPoints;
        this.pingPong = pingPong;
    }

    @Override
    public List<Vector3f> sample(ShapeContext context) {
        if (context.origin() == null || context.target() == null) {
            return new ArrayList<>();
        }

        Vector3f origin = context.origin().toVector().toVector3f();
        Vector3f target = context.target().toVector().toVector3f();

        Vector3f direction = new Vector3f(target).sub(origin);
        float length = direction.length();

        if (length <= 0f) {
            return new ArrayList<>();
        }

        Vector3f normalizedDirection = new Vector3f(direction).normalize();

        float distancePerStep = spacing * speed;
        float traveledDistance = context.step() * distancePerStep;

        List<Vector3f> result = new ArrayList<>(amountPoints);

        for (int i = 0; i < amountPoints; i++) {
            float shiftedDistance = traveledDistance - (i * spacing);

            if (shiftedDistance < 0f) {
                continue;
            }

            float distanceAlongLine;
            if (pingPong) {
                distanceAlongLine = pingPongDistance(shiftedDistance, length);
            } else {
                distanceAlongLine = shiftedDistance % length;
            }

            result.add(new Vector3f(normalizedDirection).mul(distanceAlongLine));
        }

        return result;
    }

    private float pingPongDistance(float distance, float length) {
        float wrapped = distance % (2.0f * length);
        return wrapped <= length ? wrapped : (2.0f * length) - wrapped;
    }
}