package com.github.roleplaycauldron.spellbook.effect.viewer;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * A viewer source that always returns a fixed list of viewers.
 */
public final class FixedViewerSource implements ViewerSource {

    private final List<Player> viewers;

    /**
     * Creates a new FixedViewerSource with the given list of viewers.
     *
     * @param viewers the list of viewers to always return
     */
    public FixedViewerSource(Collection<? extends Player> viewers) {
        this.viewers = List.copyOf(viewers);
    }

    @Override
    public Collection<? extends Player> resolveViewers() {
        return viewers;
    }
}