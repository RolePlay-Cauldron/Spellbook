package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import com.github.roleplaycauldron.spellbook.effect.PointBuffer;

/**
 * Represents a shape that can be used to generate particles
 */
@FunctionalInterface
public interface Shape {

    /**
     * Writes 3D points within the shape into the provided frame-local point buffer.
     * This method is typically used for generating particles or other effects
     * at specific locations within the shape.
     *
     * @param context the shape context containing information such as the step,
     *                elapsed time, origin, and target location
     * @param points  mutable frame-local point buffer to append points to
     */
    void sample(ShapeContext context, PointBuffer points);
}
