package com.github.roleplaycauldron.spellbook.effect;

import org.joml.Vector3f;

/**
 * Represents a functional interface used to populate a directional vector based on a given point and
 * rendering context. The direction vector is typically utilized in particle effects or visual transformations.
 * <p>
 * This interface is intended to provide custom logic for determining directional outputs dynamically
 * depending on the position and context at the time of calculation.
 * <p>
 * Implementations of this interface can define specific behaviors for directional responses, such as
 * - Generating a fixed directional vector
 * - Producing directions based on relative positions between origin and target in the context
 * - Applying context-based logic like time-dependent direction changes
 */
@FunctionalInterface
public interface DirectionProvider {
    /**
     * Populates the destination vector with the direction for the given local point.
     *
     * @param localX      local x coordinate
     * @param localY      local y coordinate
     * @param localZ      local z coordinate
     * @param context     effect context
     * @param destination reusable destination vector
     */
    void getDirection(float localX, float localY, float localZ, EffectContext context, Vector3f destination);
}
