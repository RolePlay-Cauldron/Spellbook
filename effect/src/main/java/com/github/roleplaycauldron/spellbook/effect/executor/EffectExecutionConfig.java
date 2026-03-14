package com.github.roleplaycauldron.spellbook.effect.executor;

import com.github.roleplaycauldron.spellbook.effect.location.EffectAnchor;
import com.github.roleplaycauldron.spellbook.effect.viewer.ViewerSource;
import org.bukkit.Location;

import java.util.function.Function;

/**
 * Represents the configuration for executing an effect, including timing parameters,
 * anchor points for origin and target, viewer source, and a step function for controlling
 * the execution flow.
 */
public final class EffectExecutionConfig {

    private final long delayTicks;

    private final long periodTicks;

    private final long maxRuns;

    private final boolean cancelIfOriginUnavailable;

    private final boolean cancelIfTargetUnavailable;

    private final boolean cancelIfWorldsDiffer;

    private final EffectAnchor originAnchor;

    private final EffectAnchor targetAnchor;

    private final ViewerSource viewerSource;

    private final Function<ExecutionFrame, Integer> stepFunction;

    private EffectExecutionConfig(Builder builder) {
        this.delayTicks = builder.delayTicks;
        this.periodTicks = builder.periodTicks;
        this.maxRuns = builder.maxRuns;
        this.cancelIfOriginUnavailable = builder.cancelIfOriginUnavailable;
        this.cancelIfTargetUnavailable = builder.cancelIfTargetUnavailable;
        this.cancelIfWorldsDiffer = builder.cancelIfWorldsDiffer;
        this.originAnchor = builder.originAnchor;
        this.targetAnchor = builder.targetAnchor;
        this.viewerSource = builder.viewerSource;
        this.stepFunction = builder.stepFunction;
    }

    /**
     * Creates and returns a new instance of the {@link Builder} class,
     * which is used for constructing {@link EffectExecutionConfig}
     * instances with custom configurations.
     *
     * @return a new {@link Builder} instance for constructing an {@link EffectExecutionConfig}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Retrieves the number of ticks to delay before starting the execution of an effect.
     *
     * @return the delay in ticks before the effect execution begins
     */
    public long delayTicks() {
        return delayTicks;
    }

    /**
     * Retrieves the interval, in ticks, between successive executions of an effect.
     *
     * @return the number of ticks representing the period between effect executions
     */
    public long periodTicks() {
        return periodTicks;
    }

    /**
     * Retrieves the maximum number of times an effect is allowed to execute.
     * A value of -1 indicates no limit on the number of executions.
     *
     * @return the maximum number of executions allowed for the effect, or -1 if unlimited
     */
    public long maxRuns() {
        return maxRuns;
    }

    /**
     * Determines whether the effect execution should be canceled if the origin location
     * is unavailable.
     *
     * @return true if the effect execution is set to cancel when the origin location
     * becomes unavailable, false otherwise
     */
    public boolean cancelIfOriginUnavailable() {
        return cancelIfOriginUnavailable;
    }

    /**
     * Determines whether the effect execution should be canceled if the target location
     * is unavailable or becomes unavailable during execution.
     *
     * @return true if the effect execution is configured to cancel when the target location
     * is unavailable, false otherwise
     */
    public boolean cancelIfTargetUnavailable() {
        return cancelIfTargetUnavailable;
    }

    /**
     * Determines whether the effect execution should be canceled if the origin and target
     * are located in different worlds.
     *
     * @return true if the effect execution is set to cancel when the origin and target
     * locations reside in different worlds, false otherwise
     */
    public boolean cancelIfWorldsDiffer() {
        return cancelIfWorldsDiffer;
    }

    /**
     * Retrieves the anchor representing the origin location for the effect execution.
     * The origin anchor provides a mechanism to dynamically resolve the starting position of an effect.
     *
     * @return the {@link EffectAnchor} defining the origin of the effect execution
     */
    public EffectAnchor originAnchor() {
        return originAnchor;
    }

    /**
     * Retrieves the anchor representing the target location for the effect execution.
     * The target anchor provides a mechanism to dynamically resolve the destination position of an effect.
     *
     * @return the {@link EffectAnchor} defining the target of the effect execution, or null if no target anchor is configured
     */
    public EffectAnchor targetAnchor() {
        return targetAnchor;
    }

    /**
     * Retrieves the {@link ViewerSource} associated with this configuration.
     * The ViewerSource defines the mechanism for resolving the set of players
     * who are eligible to view an effect during its execution. This can
     * influence which players are able to observe the visual or auditory
     * aspects of the effect.
     *
     * @return the {@link ViewerSource} responsible for determining the viewers of the effect
     */
    public ViewerSource viewerSource() {
        return viewerSource;
    }

    /**
     * Retrieves the step function associated with the execution configuration.
     * The step function is a {@link Function} that takes an {@link ExecutionFrame}
     * as input and returns an integer representing the outcome or progress
     * of the effect execution in that frame. It is used to define the behavior
     * of the effect at each execution step.
     *
     * @return the step function defining the per-frame behavior of the effect
     */
    public Function<ExecutionFrame, Integer> stepFunction() {
        return stepFunction;
    }

    /**
     * A builder class for constructing instances of {@link EffectExecutionConfig}.
     * <p>
     * This class
     */
    public static final class Builder {
        private long delayTicks = 0L;

        private long periodTicks = 1L;

        private long maxRuns = -1L;

        private boolean cancelIfOriginUnavailable = true;

        private boolean cancelIfTargetUnavailable = true;

        private boolean cancelIfWorldsDiffer = true;

        private EffectAnchor originAnchor;

        private EffectAnchor targetAnchor;

        private ViewerSource viewerSource;

        private Function<ExecutionFrame, Integer> stepFunction = frame -> (int) frame.runIndex();

        private Builder() {
        }

        /**
         * Sets the initial delay, in ticks, before the scheduled effect starts executing.
         *
         * @param delayTicks the delay in ticks; must be a non-negative value
         * @return the current {@code Builder} instance for method chaining
         * @throws IllegalArgumentException if {@code delayTicks} is less than 0
         */
        public Builder delayTicks(long delayTicks) {
            if (delayTicks < 0) throw new IllegalArgumentException("delayTicks must be >= 0");
            this.delayTicks = delayTicks;
            return this;
        }

        /**
         * Sets the period, in ticks, between successive executions of the scheduled effect.
         *
         * @param periodTicks the period in ticks; must be greater than 0
         * @return the current {@code Builder} instance for method chaining
         * @throws IllegalArgumentException if {@code periodTicks} is less than or equal to 0
         */
        public Builder periodTicks(long periodTicks) {
            if (periodTicks <= 0) throw new IllegalArgumentException("periodTicks must be > 0");
            this.periodTicks = periodTicks;
            return this;
        }

        /**
         * Sets the maximum number of times the scheduled effect will be executed.
         * A value of -1 indicates that the effect will run indefinitely.
         *
         * @param maxRuns the maximum number of runs; must be greater than 0 or -1 for infinite runs
         * @return the current {@code Builder} instance for method chaining
         * @throws IllegalArgumentException if {@code maxRuns} is 0 or less than -1
         */
        public Builder maxRuns(long maxRuns) {
            if (maxRuns == 0 || maxRuns < -1) {
                throw new IllegalArgumentException("maxRuns must be > 0 or -1 for infinite");
            }
            this.maxRuns = maxRuns;
            return this;
        }

        /**
         * Sets whether the scheduled effect should be canceled if the origin location
         * becomes unavailable during execution.
         *
         * @param cancelIfOriginUnavailable a boolean flag indicating whether the effect
         *                                  should be canceled when the origin is unavailable
         * @return the current {@code Builder} instance for method chaining
         */
        public Builder cancelIfOriginUnavailable(boolean cancelIfOriginUnavailable) {
            this.cancelIfOriginUnavailable = cancelIfOriginUnavailable;
            return this;
        }

        /**
         * Sets whether the scheduled effect should be canceled if the target location
         * becomes unavailable during execution.
         *
         * @param cancelIfTargetUnavailable a boolean flag indicating whether the effect
         *                                  should be canceled when the target is unavailable
         * @return the current {@code Builder} instance for method chaining
         */
        public Builder cancelIfTargetUnavailable(boolean cancelIfTargetUnavailable) {
            this.cancelIfTargetUnavailable = cancelIfTargetUnavailable;
            return this;
        }

        /**
         * Configures whether the scheduled effect should be canceled if the origin
         * and target locations are in different worlds during execution.
         *
         * @param cancelIfWorldsDiffer a boolean flag indicating whether the effect
         *                             should be canceled when the origin and target
         *                             are in separate worlds
         * @return the current {@code Builder} instance for method chaining
         */
        public Builder cancelIfWorldsDiffer(boolean cancelIfWorldsDiffer) {
            this.cancelIfWorldsDiffer = cancelIfWorldsDiffer;
            return this;
        }

        /**
         * Sets the origin anchor for the scheduled effect. The origin anchor determines the
         * location from which the effect originates when executed.
         *
         * @param originAnchor the {@link EffectAnchor} instance representing the origin anchor;
         *                     must not be null
         * @return the current {@code Builder} instance for method chaining
         * @throws NullPointerException if {@code originAnchor} is null
         */
        public Builder originAnchor(EffectAnchor originAnchor) {
            this.originAnchor = originAnchor;
            return this;
        }

        /**
         * Sets the target anchor for the scheduled effect. The target anchor determines the
         * location or entity on which the effect is targeted during execution.
         *
         * @param targetAnchor the {@link EffectAnchor} instance representing the target anchor;
         *                     must not be null
         * @return the current {@code Builder} instance for method chaining
         * @throws NullPointerException if {@code targetAnchor} is null
         */
        public Builder targetAnchor(EffectAnchor targetAnchor) {
            this.targetAnchor = targetAnchor;
            return this;
        }

        /**
         * Sets the viewer source for the scheduled effect. The viewer source determines
         * the collection of players who will see the effect during its execution.
         *
         * @param viewerSource the {@link ViewerSource} instance that resolves the viewers; must not be null
         * @return the current {@code Builder} instance for method chaining
         * @throws NullPointerException if {@code viewerSource} is null
         */
        public Builder viewerSource(ViewerSource viewerSource) {
            this.viewerSource = viewerSource;
            return this;
        }

        /**
         * Sets the step function for the scheduled effect. The step function determines
         * how the execution progresses at each run. It accepts an {@code ExecutionFrame}
         * as input and returns an integer that influences the subsequent execution behavior.
         *
         * @param stepFunction a {@link Function} that takes an {@link ExecutionFrame}
         *                     and returns an {@link Integer}; must not be null
         * @return the current {@code Builder} instance for method chaining
         * @throws NullPointerException if {@code stepFunction} is null
         */
        public Builder stepFunction(Function<ExecutionFrame, Integer> stepFunction) {
            this.stepFunction = stepFunction;
            return this;
        }

        /**
         * Builds and returns an instance of {@link EffectExecutionConfig} based on the
         *
         * @return an instance of {@link EffectExecutionConfig} configured with the provided parameters
         */
        public EffectExecutionConfig build() {
            if (originAnchor == null) {
                throw new IllegalStateException("originAnchor must be set");
            }

            if (viewerSource == null) {
                throw new IllegalStateException("viewerSource must be set");
            }

            return new EffectExecutionConfig(this);
        }
    }

    /**
     * Represents a single snapshot of an ongoing effect execution at a specific point in time.
     * This immutable record is used to encapsulate contextual data regarding the current state
     * of an effect execution.
     *
     * @param runIndex       The index of the current execution run. Starts at 0 and increments
     *                       with each successive run of the effect.
     * @param elapsedTicks   The total number of ticks that have elapsed since the start of the
     *                       effect execution.
     * @param elapsedSeconds The total time that has elapsed, in seconds, since the start of
     *                       the effect execution. This is computed from the elapsed ticks
     *                       based on the game's tick rate.
     * @param origin         The origin {@link Location} associated with the effect during this
     *                       execution frame. This may represent the starting point of the effect.
     * @param target         The target {@link Location} associated with the effect during this
     *                       execution frame. This may represent the destination or focus point
     *                       of the effect, or null if no target is defined.
     */
    public record ExecutionFrame(
            long runIndex,
            long elapsedTicks,
            double elapsedSeconds,
            Location origin,
            Location target
    ) {
    }
}