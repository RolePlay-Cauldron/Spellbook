package com.github.roleplaycauldron.spellbook.effect;

import org.joml.Vector3f;

/**
 * Represents the spawning context for various game-related processes, including
 * positional and directional information in both local and world space, as well
 * as additional contextual data for effects.
 *
 * @param localPoint    The point of reference in local space where the spawn occurs.
 * @param worldPoint    The point of reference in world space where the spawn occurs.
 * @param direction     The directional vector indicating the spawn's orientation or trajectory.
 * @param effectContext The effect-related context providing additional state or execution details.
 */
public record SpawnContext(
        Vector3f localPoint,
        Vector3f worldPoint,
        Vector3f direction,
        EffectContext effectContext
) {

}
