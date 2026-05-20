package com.github.roleplaycauldron.spellbook.effect;

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
     * Applies transformations or adjustments to a frame-local point buffer based on the provided effect context.
     *
     * @param points  The mutable frame-local point buffer. It may be empty but should not be null.
     * @param context The effect context providing the environmental or state-based details necessary
     *                to apply the modification. This context is expected to include information such
     *                as origin, target, step, and elapsed time.
     */
    void apply(PointBuffer points, EffectContext context);
}
