package com.github.roleplaycauldron.spellbook.effect.shape.morph;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;

/**
 * Calculates morph progress from the current shape context.
 * <p>
 * Returned values are interpreted by {@link MorphShape}. The built-in morph
 * shape clamps progress to the inclusive range {@code 0..1}, so custom progress
 * providers may return values outside that range when it is convenient.
 */
@FunctionalInterface
public interface MorphProgress {

    /**
     * Returns the same progress value for every sample.
     *
     * @param progress progress value to return
     * @return fixed progress provider
     */
    static MorphProgress fixed(float progress) {
        return context -> progress;
    }

    /**
     * Advances progress from {@code 0} to {@code 1} over render steps starting
     * at step {@code 0}.
     *
     * @param durationSteps number of render steps required to reach progress {@code 1}
     * @return step-based progress provider
     */
    static MorphProgress overSteps(int durationSteps) {
        return afterStep(0, durationSteps);
    }

    /**
     * Advances progress from {@code 0} to {@code 1} after the given start step.
     * <p>
     * Steps are render steps from {@link ShapeContext#step()}, not necessarily
     * raw server ticks.
     *
     * @param startStep     step that represents progress {@code 0}
     * @param durationSteps number of render steps required to reach progress {@code 1}
     * @return delayed step-based progress provider
     */
    static MorphProgress afterStep(int startStep, int durationSteps) {
        if (durationSteps <= 0) {
            throw new IllegalArgumentException("durationSteps must be > 0");
        }
        return context -> (context.step() - startStep) / (float) durationSteps;
    }

    /**
     * Advances progress from {@code 0} to {@code 1} over elapsed seconds
     * starting at time {@code 0}.
     *
     * @param durationSeconds seconds required to reach progress {@code 1}
     * @return time-based progress provider
     */
    static MorphProgress overSeconds(double durationSeconds) {
        return afterSeconds(0.0, durationSeconds);
    }

    /**
     * Advances progress from {@code 0} to {@code 1} after the given elapsed
     * start time.
     *
     * @param startSeconds    elapsed time that represents progress {@code 0}
     * @param durationSeconds seconds required to reach progress {@code 1}
     * @return delayed time-based progress provider
     */
    static MorphProgress afterSeconds(double startSeconds, double durationSeconds) {
        if (durationSeconds <= 0.0) {
            throw new IllegalArgumentException("durationSeconds must be > 0");
        }
        return context -> (float) ((context.timeSeconds() - startSeconds) / durationSeconds);
    }

    /**
     * Creates a triggerable step-based progress provider.
     * <p>
     * Before it is started, the provider returns {@code 0}. After start, it
     * advances according to the difference between the current step and the
     * trigger step.
     *
     * @param durationSteps number of render steps required to reach progress {@code 1}
     * @return triggerable progress provider
     */
    static TriggeredMorphProgress triggeredOverSteps(int durationSteps) {
        return new TriggeredMorphProgress(durationSteps);
    }

    /**
     * Returns morph progress for the current sample.
     *
     * @param context shape context for the current frame
     * @return progress value, usually in the range {@code 0..1}
     */
    float progress(ShapeContext context);
}
