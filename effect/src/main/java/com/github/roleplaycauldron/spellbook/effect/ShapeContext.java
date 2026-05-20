package com.github.roleplaycauldron.spellbook.effect;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Represents the contextual state of a shape used in various computations or simulations.
 */
public final class ShapeContext {

    private final List<PointBuffer> ownedScratchBuffers = new ArrayList<>();

    private final IntFunction<PointBuffer> scratchBuffers;

    private int step;

    private double timeSeconds;

    private Location origin;

    private Location target;

    private int scratchOffset;

    /**
     * Creates a new shape context.
     *
     * @param step        The current step or iteration count for the shape context.
     * @param timeSeconds The elapsed time, in seconds, relevant to the lifecycle or execution of the shape.
     * @param origin      The starting or reference location for the shape's operation.
     * @param target      The intended destination or target location for the shape's operation.
     */
    public ShapeContext(int step, double timeSeconds, Location origin, Location target) {
        this.scratchBuffers = this::ownedScratchBuffer;
        set(step, timeSeconds, origin, target);
    }

    ShapeContext(int step, double timeSeconds, Location origin, Location target, IntFunction<PointBuffer> scratchBuffers) {
        this.scratchBuffers = Objects.requireNonNull(scratchBuffers, "scratchBuffers");
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

    /**
     * Returns an indexed frame-local scratch point buffer.
     * <p>
     * Scratch buffers are reusable temporary storage owned by the current render
     * state. They are valid only during the current shape sample call and are
     * cleared before each top-level shape sample. Shapes must not retain the
     * buffer or references to its point values beyond the current sample unless
     * the values are copied.
     *
     * @param index scratch buffer index within the current scratch scope
     * @return reusable scratch point buffer
     */
    public PointBuffer scratchBuffer(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        return scratchBuffers.apply(scratchOffset + index);
    }

    /**
     * Reserves lower scratch-buffer indexes for the current compound shape while
     * child shapes are sampled.
     *
     * @param reservedBuffers number of scratch buffers reserved by the caller
     * @return scope handle that restores the previous scratch index base
     */
    public ScratchScope reserveScratchBuffers(int reservedBuffers) {
        if (reservedBuffers < 0) {
            throw new IllegalArgumentException("reservedBuffers must be >= 0");
        }
        int previousOffset = scratchOffset;
        scratchOffset += reservedBuffers;
        return new ScratchScope(previousOffset);
    }

    ShapeContext resetScratchScope() {
        scratchOffset = 0;
        return this;
    }

    private PointBuffer ownedScratchBuffer(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        while (ownedScratchBuffers.size() <= index) {
            ownedScratchBuffers.add(new PointBuffer());
        }
        return ownedScratchBuffers.get(index);
    }

    /**
     * Restores a previous scratch-buffer scope.
     */
    public final class ScratchScope implements AutoCloseable {
        private final int previousOffset;

        private boolean closed;

        private ScratchScope(int previousOffset) {
            this.previousOffset = previousOffset;
        }

        @Override
        public void close() {
            if (!closed) {
                scratchOffset = previousOffset;
                closed = true;
            }
        }
    }
}
