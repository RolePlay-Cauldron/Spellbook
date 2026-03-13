package com.github.roleplaycauldron.spellbook.effect.viewer;

import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Functional interface representing a source capable of resolving
 * a collection of viewers. The viewers retrieved through this source
 * are typically used to determine which players are eligible to observe
 * various effects, such as visual or auditory components within a system.
 * <p>
 * Implementations of this interface define the specific logic for how
 * the viewers are determined and returned.
 */
@FunctionalInterface
public interface ViewerSource {

    /**
     * Resolves and retrieves the collection of viewers associated with this source.
     * The returned collection typically represents the players who are eligible
     * to observe an effect, such as visual or auditory components, as determined
     * by the specific implementation of the ViewerSource.
     *
     * @return a collection of players representing the viewers for the effect
     */
    Collection<? extends Player> resolveViewers();
}