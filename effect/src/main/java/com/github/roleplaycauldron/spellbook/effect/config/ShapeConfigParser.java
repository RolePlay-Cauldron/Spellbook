package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Parses a configured shape component.
 */
@FunctionalInterface
public interface ShapeConfigParser {

    /**
     * Parses a shape from the given section.
     *
     * @param section shape configuration section
     * @param context parser context for nested parsing and path-aware errors
     * @return parsed shape
     */
    Shape parse(ConfigurationSection section, EffectConfigContext context);
}
