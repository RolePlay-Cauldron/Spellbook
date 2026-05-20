package com.github.roleplaycauldron.spellbook.effect.emitter;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;

/**
 * Represents an entity capable of spawning particles in a specific context.
 * <p>
 * Implementations of this interface define the logic for spawning particles
 * based on frame-local point data. The spawned particles can vary
 * in type, count, location, and other characteristics depending on the implementation.
 */
@FunctionalInterface
public interface ParticleEmitter {

    /**
     * Returns whether this emitter consumes per-point direction values.
     * Emitters that do not need direction data can use the default to let the render
     * pipeline avoid unnecessary direction calculations.
     *
     * @return {@code true} when direction values are required by {@link #spawn}
     */
    default boolean requiresDirection() {
        return true;
    }

    /**
     * Spawns particles based on the provided point data.
     * The implementation determines the specific behavior of the particle spawning,
     * such as the type, quantity, and configuration of particles.
     *
     * @param context    effect context
     * @param localX     local x coordinate
     * @param localY     local y coordinate
     * @param localZ     local z coordinate
     * @param worldX     world x coordinate
     * @param worldY     world y coordinate
     * @param worldZ     world z coordinate
     * @param directionX direction x value
     * @param directionY direction y value
     * @param directionZ direction z value
     */
    void spawn(
            EffectContext context,
            float localX,
            float localY,
            float localZ,
            double worldX,
            double worldY,
            double worldZ,
            float directionX,
            float directionY,
            float directionZ
    );
}
