package com.github.roleplaycauldron.spellbook.effect.location;

import org.bukkit.Location;

/**
 * Represents a functional interface for resolving a {@link Location}.
 * An implementation of this interface defines how to dynamically or statically
 * determine the location for effects or other operations that require positional data.
 * <p>
 * The {@code EffectAnchor} interface can be implemented to provide location data
 * in various ways, such as resolving the position of an entity or providing
 * a fixed location.
 * <p>
 * Implementations of this interface are expected to define the logic for how
 * the location is determined when the {@link #resolve()} method is called.
 */
@FunctionalInterface
public interface EffectAnchor {

    /**
     * Resolves and returns the {@link Location} associated with this anchor.
     * The way the location is determined depends on the implementation of the {@link EffectAnchor}.
     * This method may return a dynamically computed, statically defined, or entity-based location.
     *
     * @return the resolved {@link Location}, or {@code null} if the location could not be determined
     */
    Location resolve();
}
