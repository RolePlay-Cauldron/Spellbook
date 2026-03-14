package com.github.roleplaycauldron.spellbook.effect;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * Represents the contextual information required to render or execute an effect.
 * This context is typically used to define the state and scope of an effect
 * during its execution in a game world.
 *
 * @param world       The world in which the effect is being executed.
 * @param origin      The origin location where the effect starts or is centered.
 * @param target      The target location the effect is intended to reach or interact with.
 * @param viewers     A collection of players who can see or are aware of this effect.
 *                    If null, an empty immutable list is assigned by default.
 * @param step        The current step index of the effect process, usually for sequential effects.
 * @param tick        The server tick count when this context is created or used, allowing
 *                    time-sensitive actions to align with game mechanics.
 * @param timeSeconds The elapsed time in seconds that can be used for time-based calculations.
 */
public record EffectContext(
        World world,
        Location origin,
        Location target,
        Collection<? extends Player> viewers,
        int step,
        long tick,
        double timeSeconds
) {
    /**
     * Initializes the EffectContext record, ensuring that the collection
     * of viewers is never null. If a null value is provided for the viewers,
     * it is replaced with an empty immutable list.
     *
     * @param world       The world in which the effect is being executed.
     * @param origin      The origin location where the effect starts or is centered.
     * @param target      The target location the effect is intended to reach or interact with.
     * @param viewers     A collection of players who can see or are aware of this effect.
     *                    If null, it will be set as an empty immutable list.
     * @param step        The current step index of the effect process, usually for sequential effects.
     * @param tick        The server tick count when this context is created or used, ensuring
     *                    alignment with game mechanics in time-sensitive actions.
     * @param timeSeconds The elapsed time in seconds used for time-based calculations.
     */
    public EffectContext {
        viewers = viewers == null ? List.of() : List.copyOf(viewers);
    }
}