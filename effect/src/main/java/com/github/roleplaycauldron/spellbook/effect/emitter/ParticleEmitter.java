package com.github.roleplaycauldron.spellbook.effect.emitter;

import com.github.roleplaycauldron.spellbook.effect.SpawnContext;

/**
 * Represents an entity capable of spawning particles in a specific context.
 * <p>
 * Implementations of this interface define the logic for spawning particles
 * based on the provided {@link SpawnContext}. The spawned particles can vary
 * in type, count, location, and other characteristics depending on the implementation.
 */
@FunctionalInterface
public interface ParticleEmitter {

    /**
     * Spawns particles based on the provided {@link SpawnContext}.
     * The implementation determines the specific behavior of the particle spawning,
     * such as the type, quantity, and configuration of particles.
     *
     * @param context the context in which the particle spawning occurs, containing
     *                information such as the local and world coordinates, direction,
     *                and effect-specific metadata
     */
    void spawn(SpawnContext context);
}
