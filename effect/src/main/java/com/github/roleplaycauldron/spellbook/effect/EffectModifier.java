package com.github.roleplaycauldron.spellbook.effect;

import org.joml.Vector3f;

import java.util.List;

/**
 * Represents a modifier that can apply transformations or adjustments to a collection of 3D points
 * within the context of an effect.
 * <p>
 * This functional interface is typically used to enable customization or dynamic modifications
 * of shapes or particle positions for visual effects based on the provided effect context.
 */
@FunctionalInterface
public interface EffectModifier {

    /**
     * Applies transformations or adjustments to a list of 3D points based on the provided effect context.
     *
     * @param points  The list of 3D points (represented as {@link Vector3f}) to be modified.
     *                It may be empty but should not be null.
     * @param context The effect context providing the environmental or state-based details necessary
     *                to apply the modification. This context is expected to include information such
     *                as origin, target, step, and elapsed time.
     * @return A new list of transformed or adjusted 3D points. The resulting list will maintain the
     * same size as the input list but may contain modified or replaced points based on the effect logic.
     */
    List<Vector3f> apply(List<Vector3f> points, EffectContext context);
}
