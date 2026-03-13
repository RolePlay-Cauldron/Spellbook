package com.github.roleplaycauldron.spellbook.effect.transform;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import org.joml.Vector3f;

/**
 * Represents a transformation that changes the direction of a point based on a target location.
 */
@FunctionalInterface
public interface Transform {

    /**
     * Applies a transformation to a given point within the context of an effect.
     *
     * @param point   the 3D vector representing the point to be transformed
     * @param context the effect context containing relevant information, such as
     *                the origin, target location, current tick, and associated viewers
     * @return a new vector representing the transformed point
     */
    Vector3f apply(Vector3f point, EffectContext context);
}
