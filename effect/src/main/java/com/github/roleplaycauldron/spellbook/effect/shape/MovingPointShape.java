package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a moving point along a line defined by an origin and a target location.
 * The movement of the point is determined by a given speed and an optional ping-pong behavior.
 * This class samples a single 3D point at a specific step, calculated based on its progress.
 * The sampled point is positioned along the line between the origin and the target in 3D space.
 */
public final class MovingPointShape implements Shape {

    private final float speed;

    private final boolean pingPong;

    /**
     * Constructs a new instance of the MovingPointShape class.
     *
     * @param speed    the speed of the moving point; must be greater than 0
     * @param pingPong whether the point should ping-pong back and forth along the line
     * @throws IllegalArgumentException if the speed is less than or equal to 0
     */
    public MovingPointShape(float speed, boolean pingPong) {
        if (speed <= 0f) {
            throw new IllegalArgumentException("speed must be > 0");
        }

        this.speed = speed;
        this.pingPong = pingPong;
    }

    @Override
    public List<Vector3f> sample(ShapeContext context) {
        if (context.origin() == null || context.target() == null) {
            return new ArrayList<>();
        }
        Vector3f origin = context.origin().toVector().toVector3f();
        Vector3f target = context.target().toVector().toVector3f();

        float progress = context.step() * speed;
        float t = pingPong ? pingPong(progress) : (progress % 1.0f);

        Vector3f direction = new Vector3f(target).sub(origin);
        Vector3f point = direction.mul(t);

        List<Vector3f> result = new ArrayList<>(1);
        result.add(point);
        return result;
    }

    private float pingPong(float value) {
        float wrapped = value % 2.0f;
        return wrapped <= 1.0f ? wrapped : 2.0f - wrapped;
    }
}