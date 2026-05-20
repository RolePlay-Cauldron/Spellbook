package com.github.roleplaycauldron.spellbook.effect.transform;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import org.bukkit.Location;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A transformation that aligns a point's orientation to face a target location.
 * <p>
 * <p>The transformation calculates a direction vector pointing from
 * the origin to the target location within the given context. This
 * direction vector is then used to compute a rotation aligning a
 * predefined forward axis to the computed direction. The resulting
 * rotation is applied to the given point to produce the transformed
 * point.</p>
 *
 * <p>If no target location is provided in the context, or if the
 * direction vector between the origin and target is negligible
 * (length squared is below a small threshold), no transformation
 * is applied. The input point is returned as is.</p>
 *
 * <p>This transformation provides a flexible way to manipulate
 * the orientation of effect points in 3D space, useful in applications
 * like particle effects, visualizations, or other spatial dynamics.</p>
 */
public class LookAtTransform implements Transform {

    private final Vector3f forwardAxis;

    /**
     * Creates a new LookAtTransform with a default forward axis of (0, 1, 0).
     */
    public LookAtTransform() {
        this(new Vector3f(0, 1, 0));
    }

    /**
     * Creates a new LookAtTransform with a custom forward axis.
     *
     * @param forwardAxis The forward axis vector for the transformation.
     */
    public LookAtTransform(Vector3f forwardAxis) {
        this.forwardAxis = new Vector3f(forwardAxis).normalize();
    }

    @Override
    public PreparedTransform prepare(EffectContext context) {
        Location origin = context.origin();
        Location target = context.target();

        if (target == null) {
            return (points, index) -> {
            };
        }

        Vector3f direction = new Vector3f(
                (float) (target.getX() - origin.getX()),
                (float) (target.getY() - origin.getY()),
                (float) (target.getZ() - origin.getZ())
        );

        if (direction.lengthSquared() < 1e-6) {
            return (points, index) -> {
            };
        }

        direction.normalize();
        Quaternionf rotation = new Quaternionf().rotateTo(
                forwardAxis.x, forwardAxis.y, forwardAxis.z,
                direction.x, direction.y, direction.z
        );

        Vector3f point = new Vector3f();
        return (points, index) -> {
            points.get(index, point);
            rotation.transform(point);
            points.set(index, point.x, point.y, point.z);
        };
    }

    @Override
    public void apply(PointBuffer points, int index, EffectContext context) {
        prepare(context).apply(points, index);
    }
}
