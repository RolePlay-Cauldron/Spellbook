package com.github.roleplaycauldron.spellbook.effect;

import org.bukkit.Location;

/**
 * Represents the contextual state of a shape used in various computations or simulations.
 */
public final class ShapeContext {

    private int step;

    private double timeSeconds;

    private Location origin;

    private Location target;

    /**
     * Creates a new shape context.
     *
     * @param step        The current step or iteration count for the shape context.
     * @param timeSeconds The elapsed time, in seconds, relevant to the lifecycle or execution of the shape.
     * @param origin      The starting or reference location for the shape's operation.
     * @param target      The intended destination or target location for the shape's operation.
     */
    public ShapeContext(int step, double timeSeconds, Location origin, Location target) {
        set(step, timeSeconds, origin, target);
    }

    ShapeContext set(int step, double timeSeconds, Location origin, Location target) {
        this.step = step;
        this.timeSeconds = timeSeconds;
        this.origin = origin;
        this.target = target;
        return this;
    }

    public int step() {
        return step;
    }

    public double timeSeconds() {
        return timeSeconds;
    }

    public Location origin() {
        return origin;
    }

    public Location target() {
        return target;
    }
}
