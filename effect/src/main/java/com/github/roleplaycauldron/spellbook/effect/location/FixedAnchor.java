package com.github.roleplaycauldron.spellbook.effect.location;

import org.bukkit.Location;

/**
 * Represents a fixed anchor point in the world.
 */
public final class FixedAnchor implements EffectAnchor {
    private final Location location;

    /**
     * Creates a new FixedAnchor
     *
     * @param location the location to anchor to
     */
    public FixedAnchor(Location location) {
        this.location = location.clone();
    }
    
    @Override
    public Location resolve() {
        return location.clone();
    }
}
