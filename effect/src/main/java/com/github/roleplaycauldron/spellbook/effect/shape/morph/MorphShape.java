package com.github.roleplaycauldron.spellbook.effect.shape.morph;

import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;

import java.util.Objects;

/**
 * Shape that interpolates sampled points from a source shape to a target shape.
 * <p>
 * A morph shape samples both child shapes with the current {@link ShapeContext},
 * pairs their sampled points through a {@link MorphPointStrategy}, clamps the
 * configured {@link MorphProgress} to {@code 0..1}, and appends interpolated
 * local-space points to the provided output buffer.
 * <p>
 * Temporary child samples are stored in frame-local scratch buffers obtained
 * from {@link ShapeContext#scratchBuffer(int)}. The morph shape does not retain
 * sampled point data between frames.
 */
public final class MorphShape implements Shape {

    private final Shape source;

    private final Shape target;

    private final MorphProgress progress;

    private final MorphPointStrategy strategy;

    private MorphShape(Builder builder) {
        this.source = Objects.requireNonNull(builder.source, "source");
        this.target = Objects.requireNonNull(builder.target, "target");
        this.progress = Objects.requireNonNull(builder.progress, "progress");
        this.strategy = Objects.requireNonNull(builder.strategy, "strategy");
    }

    /**
     * Creates a builder for a morph between two shapes.
     *
     * @param source shape sampled at progress {@code 0}
     * @param target shape sampled at progress {@code 1}
     * @return morph shape builder
     */
    public static Builder between(Shape source, Shape target) {
        return new Builder(source, target);
    }

    private static float clamp(float progress) {
        if (progress < 0f) {
            return 0f;
        }
        if (progress > 1f) {
            return 1f;
        }
        return progress;
    }

    private static float lerp(float source, float target, float progress) {
        return source + (target - source) * progress;
    }

    /**
     * Samples the source and target shapes, interpolates paired points, and
     * appends the morphed points to {@code points}.
     * <p>
     * If either child shape produces no points, this method appends no points.
     * Progress values below {@code 0} are treated as {@code 0}; values above
     * {@code 1} are treated as {@code 1}.
     *
     * @param context current shape context
     * @param points  output point buffer to append morphed points to
     */
    @Override
    public void sample(ShapeContext context, PointBuffer points) {
        PointBuffer sourcePoints = context.scratchBuffer(0);
        PointBuffer targetPoints = context.scratchBuffer(1);
        sourcePoints.clear();
        targetPoints.clear();

        try (ShapeContext.ScratchScope ignored = context.reserveScratchBuffers(2)) {
            source.sample(context, sourcePoints);
            target.sample(context, targetPoints);
        }

        int sourceSize = sourcePoints.size();
        int targetSize = targetPoints.size();
        if (sourceSize == 0 || targetSize == 0) {
            return;
        }

        float clampedProgress = clamp(progress.progress(context));
        int outputSize = strategy.outputSize(sourceSize, targetSize);
        points.ensureCapacity(points.size() + outputSize);
        for (int i = 0; i < outputSize; i++) {
            int sourceIndex = strategy.sourceIndex(i, outputSize, sourceSize);
            int targetIndex = strategy.targetIndex(i, outputSize, targetSize);
            float x = lerp(sourcePoints.x(sourceIndex), targetPoints.x(targetIndex), clampedProgress);
            float y = lerp(sourcePoints.y(sourceIndex), targetPoints.y(targetIndex), clampedProgress);
            float z = lerp(sourcePoints.z(sourceIndex), targetPoints.z(targetIndex), clampedProgress);
            points.add(x, y, z);
        }
    }

    /**
     * Builder for {@link MorphShape}.
     */
    public static final class Builder {
        private final Shape source;

        private final Shape target;

        private MorphProgress progress = MorphProgress.fixed(0f);

        private MorphPointStrategy strategy = MorphPointStrategies.matchIndex();

        private Builder(Shape source, Shape target) {
            this.source = Objects.requireNonNull(source, "source");
            this.target = Objects.requireNonNull(target, "target");
        }

        /**
         * Sets the progress provider used for the morph.
         *
         * @param progress progress provider to evaluate during sampling
         * @return this builder
         */
        public Builder progress(MorphProgress progress) {
            this.progress = Objects.requireNonNull(progress, "progress");
            return this;
        }

        /**
         * Uses a fixed progress value for every sample.
         *
         * @param progress progress value, clamped by {@link MorphShape} during sampling
         * @return this builder
         */
        public Builder fixedProgress(float progress) {
            return progress(MorphProgress.fixed(progress));
        }

        /**
         * Morphs from source to target over the given number of render steps,
         * starting at step {@code 0}.
         *
         * @param durationSteps number of render steps required to reach progress {@code 1}
         * @return this builder
         */
        public Builder overSteps(int durationSteps) {
            return progress(MorphProgress.overSteps(durationSteps));
        }

        /**
         * Morphs from source to target after a step delay.
         *
         * @param startStep     first render step that should represent progress {@code 0}
         * @param durationSteps number of render steps required to reach progress {@code 1}
         * @return this builder
         */
        public Builder afterStep(int startStep, int durationSteps) {
            return progress(MorphProgress.afterStep(startStep, durationSteps));
        }

        /**
         * Morphs from source to target over the given elapsed time, starting at
         * time {@code 0}.
         *
         * @param durationSeconds seconds required to reach progress {@code 1}
         * @return this builder
         */
        public Builder overSeconds(double durationSeconds) {
            return progress(MorphProgress.overSeconds(durationSeconds));
        }

        /**
         * Morphs from source to target after an elapsed-time delay.
         *
         * @param startSeconds    elapsed time that should represent progress {@code 0}
         * @param durationSeconds seconds required to reach progress {@code 1}
         * @return this builder
         */
        public Builder afterSeconds(double startSeconds, double durationSeconds) {
            return progress(MorphProgress.afterSeconds(startSeconds, durationSeconds));
        }

        /**
         * Sets the point pairing or resampling strategy.
         *
         * @param strategy strategy used to choose output point count and source/target indexes
         * @return this builder
         */
        public Builder strategy(MorphPointStrategy strategy) {
            this.strategy = Objects.requireNonNull(strategy, "strategy");
            return this;
        }

        /**
         * Creates the configured morph shape.
         *
         * @return morph shape
         */
        public MorphShape build() {
            return new MorphShape(this);
        }
    }
}
