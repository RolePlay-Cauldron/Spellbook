package com.github.roleplaycauldron.spellbook.effect;

import org.bukkit.Location;

/**
 * Represents the contextual state of a shape used in various computations or simulations.
 * This record encapsulates step data, timing information, and origin/target locations.
 *
 * @param step        The current step or iteration count for the shape context, helpful in determining progression or states.
 * @param timeSeconds The elapsed time, in seconds, relevant to the lifecycle or execution of the shape.
 * @param origin      The starting or reference location for the shape's operation.
 * @param target      The intended destination or target location for the shape's operation.
 */
public record ShapeContext(
        int step,
        double timeSeconds,
        Location origin,
        Location target
) {
}