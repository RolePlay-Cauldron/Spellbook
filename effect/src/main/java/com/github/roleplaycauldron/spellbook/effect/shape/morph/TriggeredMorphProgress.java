package com.github.roleplaycauldron.spellbook.effect.shape.morph;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;

/**
 * Step-based progress provider that starts when explicitly triggered.
 * <p>
 * Instances are mutable trigger handles. If the same effect definition is
 * rendered by multiple independent executions and each execution needs separate
 * trigger timing, create one progress instance per execution.
 */
public final class TriggeredMorphProgress implements MorphProgress {

    private final int durationSteps;

    private Integer startStep;

    TriggeredMorphProgress(int durationSteps) {
        if (durationSteps <= 0) {
            throw new IllegalArgumentException("durationSteps must be > 0");
        }
        this.durationSteps = durationSteps;
    }

    /**
     * Starts the morph using the current frame step as progress zero.
     *
     * @param context shape context from the frame that triggers the morph
     */
    public void start(ShapeContext context) {
        start(context.step());
    }

    /**
     * Starts the morph at the provided step.
     *
     * @param step trigger step
     */
    public void start(int step) {
        startStep = step;
    }

    /**
     * Resets this progress provider to its unstarted state.
     */
    public void reset() {
        startStep = null;
    }

    /**
     * Returns whether this progress provider has been started.
     *
     * @return {@code true} after {@link #start(int)} or {@link #start(ShapeContext)}
     */
    public boolean started() {
        return startStep != null;
    }

    /**
     * Returns {@code 0} before start, then advances by render steps from the
     * trigger step.
     *
     * @param context shape context for the current frame
     * @return step-based progress value
     */
    @Override
    public float progress(ShapeContext context) {
        if (startStep == null) {
            return 0f;
        }
        return (context.step() - startStep) / (float) durationSteps;
    }
}
