package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.List;

/**
 * Represents a shape that can be used to generate particles
 */
@FunctionalInterface
public interface Shape {

    /**
     * Generates a list of 3D points within the shape based on the provided context.
     * This method is typically used for generating particles or other effects
     * at specific locations within the shape.
     *
     * @param context the shape context containing information such as the step,
     *                elapsed time, origin, and target location
     * @return a list of 3D vectors representing points within the shape
     */
    List<Vector3f> sample(ShapeContext context);
}