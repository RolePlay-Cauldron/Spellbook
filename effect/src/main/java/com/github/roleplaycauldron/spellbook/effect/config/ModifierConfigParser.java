package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.EffectModifier;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Parses a configured effect modifier component.
 */
@FunctionalInterface
public interface ModifierConfigParser {

    /**
     * Parses an effect modifier from the given section.
     *
     * @param section modifier configuration section
     * @param context parser context for nested parsing and path-aware errors
     * @return parsed effect modifier
     */
    EffectModifier parse(ConfigurationSection section, EffectConfigContext context);
}
