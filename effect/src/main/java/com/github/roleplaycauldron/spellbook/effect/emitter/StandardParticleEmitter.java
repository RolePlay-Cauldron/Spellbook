package com.github.roleplaycauldron.spellbook.effect.emitter;

import com.github.roleplaycauldron.spellbook.effect.ParticleSpec;
import com.github.roleplaycauldron.spellbook.effect.SpawnContext;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * A standard implementation of the {@link ParticleEmitter} interface that spawns particles
 * based on a given {@link ParticleSpec} configuration.
 *
 * @param <T> The type of data associated with the particle, which can be used for additional customization.
 */
public class StandardParticleEmitter<T> implements ParticleEmitter {

    private final ParticleSpec<T> spec;

    /**
     * Creates a new StandardParticleEmitter with the given {@link ParticleSpec}
     *
     * @param spec The {@link ParticleSpec} defining the particle's properties
     */
    public StandardParticleEmitter(ParticleSpec<T> spec) {
        this.spec = spec;
    }

    /**
     * Creates a new {@link StandardParticleEmitter} instance configured with the given {@link Particle}.
     * This method simplifies the creation of a particle emitter by using a default {@link ParticleSpec}
     * with the specified particle and default parameters.
     *
     * @param particle the particle type to be used for the emitter
     * @return a {@link StandardParticleEmitter} instance configured with the provided particle
     */
    public static StandardParticleEmitter<Void> of(Particle particle) {
        return new StandardParticleEmitter<>(new ParticleSpec<>(particle));
    }

    /**
     * Creates a new {@link StandardParticleEmitter} instance configured with the given {@link Particle}
     * and the specified particle count. This method simplifies the creation of a particle emitter
     * by using a default {@link ParticleSpec} with the provided particle and count.
     *
     * @param particle the particle type to be used for the emitter
     * @param count    the number of particles to emit
     * @return a {@link StandardParticleEmitter} instance configured with the specified particle and count
     */
    public static StandardParticleEmitter<Void> of(Particle particle, int count) {
        return new StandardParticleEmitter<>(new ParticleSpec<>(particle, count));
    }

    @Override
    public void spawn(SpawnContext context) {
        Collection<? extends Player> viewers = context.effectContext().viewers();
        if (viewers.isEmpty()) {
            return;
        }

        Location location = new Location(
                context.effectContext().world(),
                context.worldPoint().x(),
                context.worldPoint().y(),
                context.worldPoint().z()
        );

        for (Player viewer : viewers) {
            viewer.spawnParticle(
                    spec.particle(),
                    location,
                    spec.count(),
                    spec.offsetX(),
                    spec.offsetY(),
                    spec.offsetZ(),
                    spec.extra(),
                    spec.data()
            );
        }
    }
}
