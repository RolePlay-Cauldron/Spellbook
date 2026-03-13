package com.github.roleplaycauldron.spellbook.effect.location;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Represents an anchor point resolved to the current location of an entity.
 * If the entity changes its location, the anchor will update accordingly.
 */
public final class EntityAnchor implements EffectAnchor {
    private final Entity entity;

    /**
     * Creates a new EntityAnchor
     *
     * @param entity the entity to anchor to
     */
    public EntityAnchor(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Location resolve() {
        return entity.isValid() ? entity.getLocation().clone() : null;
    }
}
