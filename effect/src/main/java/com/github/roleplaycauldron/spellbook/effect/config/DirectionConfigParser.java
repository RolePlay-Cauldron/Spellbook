package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.DirectionProvider;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Parses a configured direction provider component.
 */
@FunctionalInterface
public interface DirectionConfigParser {

    /**
     * Parses a direction provider from the given section.
     *
     * @param section direction configuration section
     * @param context parser context for nested parsing and path-aware errors
     * @return parsed direction provider
     */
    DirectionProvider parse(ConfigurationSection section, EffectConfigContext context);
}
