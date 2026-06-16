package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.transform.Transform;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Parses a configured transform component.
 */
@FunctionalInterface
public interface TransformConfigParser {

    /**
     * Parses a transform from the given section.
     *
     * @param section transform configuration section
     * @param context parser context for nested parsing and path-aware errors
     * @return parsed transform
     */
    Transform parse(ConfigurationSection section, EffectConfigContext context);
}
