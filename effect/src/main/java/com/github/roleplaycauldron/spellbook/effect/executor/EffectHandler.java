package com.github.roleplaycauldron.spellbook.effect.executor;

import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Interface for handling the execution of effects within the Spellbook framework.
 */
public interface EffectHandler {

    /**
     * Plays an effect at a specific location for a collection of viewers.
     *
     * @param effect   effect to play
     * @param position location to play effect at
     * @param viewers  viewers to play effect to
     * @param config   effect execution configuration
     * @return a {@link RunningEffect} representing the ongoing execution of the effect
     */
    RunningEffect playAt(
            EffectInstance effect,
            Location position,
            Collection<? extends Player> viewers,
            EffectExecutionConfig config
    );

    /**
     * Plays an effect between two locations for a collection of viewers.
     *
     * @param effect  effect to play
     * @param from    1st location to play effect from
     * @param to      2nd location to play effect to
     * @param viewers viewers to play effect to
     * @param config  effect execution configuration
     * @return a {@link RunningEffect} representing the ongoing execution of the effect
     */
    RunningEffect playBetweenPoints(
            EffectInstance effect,
            Location from,
            Location to,
            Collection<? extends Player> viewers,
            EffectExecutionConfig config
    );

    /**
     * Plays an effect between two entities for a collection of viewers.
     *
     * @param effect  effect to play
     * @param from    1st entity to play effect from
     * @param to      2nd entity to play effect to
     * @param viewers viewers to play effect to
     * @param config  effect execution configuration
     * @return a {@link RunningEffect} representing the ongoing execution of the effect
     */
    RunningEffect playBetweenEntities(
            EffectInstance effect,
            Entity from,
            Entity to,
            Collection<? extends Player> viewers,
            EffectExecutionConfig config
    );

    /**
     * Plays an effect from an entity to a specific location for a collection of viewers.
     *
     * @param effect  effect to play
     * @param from    entity to play effect from
     * @param to      location to play effect to
     * @param viewers viewers to play effect to
     * @param config  effect execution configuration
     * @return a {@link RunningEffect} representing the ongoing execution of the effect
     */
    RunningEffect playFromEntityToPoint(
            EffectInstance effect,
            Entity from,
            Location to,
            Collection<? extends Player> viewers,
            EffectExecutionConfig config
    );
}