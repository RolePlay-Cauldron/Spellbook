package com.github.roleplaycauldron.spellbook.effect.config;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Parses optional Bukkit particle data for a standard particle emitter.
 * <p>
 * Default parsing intentionally supports only flat standard particle fields.
 * Callers can register particle data parsers for Bukkit data types such as
 * dust, block, or item data when their plugin needs them.
 */
@FunctionalInterface
public interface ParticleDataConfigParser {

    /**
     * Parses particle data for the configured particle.
     *
     * @param particle Bukkit particle type being configured
     * @param section  particle data configuration section
     * @param context  parser context for nested parsing and path-aware errors
     * @return parsed data object for {@link com.github.roleplaycauldron.spellbook.effect.ParticleSpec}
     */
    Object parse(Particle particle, ConfigurationSection section, EffectConfigContext context);
}
