package com.github.roleplaycauldron.spellbook.effect.transform;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * <p> A transformation that applies a rotation to a 3D point using a quaternion.</p>
 *
 * <p>This class represents a rotational transformation in 3D space, defined
 * either by a quaternion or by Euler angles (yaw, pitch, and roll). The
 * transformation is applied to a given point, altering its orientation
 * according to the specified rotation.</p>
 *
 * <p>Rotations are constructed using the {@code Quaternionf} class from the
 * JOML library, ensuring robust mathematical operations for 3D transformations.</p>
 */
public class RotationTransform implements Transform {

    private final Quaternionf rotation;

    /**
     * Creates a new RotationTransform with the given quaternion rotation.
     *
     * @param rotation the quaternion representing the rotation to apply
     */
    public RotationTransform(Quaternionf rotation) {
        this.rotation = new Quaternionf(rotation);
    }

    /**
     * Creates a new RotationTransform with Euler angles (yaw, pitch, and roll).
     *
     * @param yaw   the yaw angle in degrees
     * @param pitch the pitch angle in degrees
     * @param roll  the roll angle in degrees
     */
    public RotationTransform(float yaw, float pitch, float roll) {
        this.rotation = new Quaternionf().rotateYXZ(
                (float) Math.toRadians(yaw),
                (float) Math.toRadians(pitch),
                (float) Math.toRadians(roll)
        );
    }

    @Override
    public Vector3f apply(Vector3f point, EffectContext context) {
        Vector3f result = new Vector3f(point);
        rotation.transform(result);
        return result;
    }
}
