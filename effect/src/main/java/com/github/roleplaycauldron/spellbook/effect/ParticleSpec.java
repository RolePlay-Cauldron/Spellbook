package com.github.roleplaycauldron.spellbook.effect;

import org.bukkit.Particle;

/**
 * Represents the specification for spawning particles in a configurable way.
 * This record allows precise control over the type, quantity, offsets, extra data,
 * and additional information required when spawning particles.
 *
 * @param <T>      The type of data associated with the particle, can be specialized as needed.
 * @param particle The particle type to spawn.
 * @param count    The number of particles to spawn.
 * @param offsetX  The x-axis offset for the particle.
 * @param offsetY  The y-axis offset for the particle.
 * @param offsetZ  The z-axis offset for the particle.
 * @param extra    An additional parameter for extra particle behavior.
 * @param data     Optional additional data associated with the particle type.
 */
public record ParticleSpec<T>(
        Particle particle,
        int count,
        double offsetX,
        double offsetY,
        double offsetZ,
        double extra,
        T data
) {
    /**
     * Constructs a particle specification with default parameters.
     *
     * @param particle The particle type to spawn.
     */
    public ParticleSpec(Particle particle) {
        this(particle, 1, 0, 0, 0, 0, null);
    }

    /**
     * Constructs a particle specification with configurable particle type and count.
     *
     * @param particle The particle type to spawn.
     * @param count    The number of particles to spawn.
     */
    public ParticleSpec(Particle particle, int count) {
        this(particle, count, 0, 0, 0, 0, null);
    }
}
