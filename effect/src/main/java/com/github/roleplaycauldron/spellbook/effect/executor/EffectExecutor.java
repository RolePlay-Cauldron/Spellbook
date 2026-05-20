package com.github.roleplaycauldron.spellbook.effect.executor;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import com.github.roleplaycauldron.spellbook.effect.EffectRenderState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

/**
 * Executor for managing and executing effects within the Spellbook framework.
 */
public final class EffectExecutor {

    private final JavaPlugin plugin;

    /**
     * Creates a new EffectExecutor.
     *
     * @param plugin the {@link JavaPlugin} instance that will be used to register
     */
    public EffectExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts executing an effect with the given configuration.
     *
     * @param effect the {@link EffectInstance} to execute
     * @param config the {@link EffectExecutionConfig} for the execution
     * @return a {@link RunningEffect} representing the ongoing execution
     */
    public RunningEffect start(EffectInstance effect, EffectExecutionConfig config) {
        BukkitRunnable runnable = new BukkitRunnable() {

            private long runIndex = 0L;

            private final EffectRenderState renderState = new EffectRenderState();

            @Override
            public void run() {
                if (config.maxRuns() != -1 && runIndex >= config.maxRuns()) {
                    cancel();
                    return;
                }

                FrameResult result = renderFrame(effect, config, runIndex, renderState);
                if (result.cancel()) {
                    cancel();
                }
                if (result.advance()) {
                    runIndex++;
                }
            }
        };

        BukkitTask task = runnable.runTaskTimer(
                plugin,
                config.delayTicks(),
                config.periodTicks()
        );

        return new RunningEffect(task);
    }

    static FrameResult renderFrame(
            EffectInstance effect,
            EffectExecutionConfig config,
            long runIndex,
            EffectRenderState renderState
    ) {
        Location origin = config.originAnchor().resolve();

        if (origin == null || origin.getWorld() == null) {
            return new FrameResult(false, config.cancelIfOriginUnavailable());
        }

        Location target = null;
        if (config.targetAnchor() != null) {
            target = config.targetAnchor().resolve();

            if (target == null || target.getWorld() == null) {
                return new FrameResult(false, config.cancelIfTargetUnavailable());
            }

            if (!origin.getWorld().equals(target.getWorld())) {
                return new FrameResult(false, config.cancelIfWorldsDiffer());
            }
        }

        Collection<? extends Player> viewers = config.viewerSource().resolveViewers();

        long elapsedTicks = runIndex * config.periodTicks();
        double elapsedSeconds = elapsedTicks / 20.0;

        Location contextOrigin = origin.clone();
        Location contextTarget = target == null ? null : target.clone();
        int step = calculateStep(config, runIndex, elapsedTicks, elapsedSeconds, contextOrigin, contextTarget);

        EffectContext context = new EffectContext(
                origin.getWorld(),
                contextOrigin,
                contextTarget,
                viewers,
                step,
                elapsedTicks,
                elapsedSeconds
        );

        effect.render(context, renderState);
        return new FrameResult(true, false);
    }

    private static int calculateStep(
            EffectExecutionConfig config,
            long runIndex,
            long elapsedTicks,
            double elapsedSeconds,
            Location origin,
            Location target
    ) {
        if (config.usesDefaultStepFunction()) {
            return (int) runIndex;
        }

        EffectExecutionConfig.ExecutionFrame frame =
                new EffectExecutionConfig.ExecutionFrame(
                        runIndex,
                        elapsedTicks,
                        elapsedSeconds,
                        origin.clone(),
                        target == null ? null : target.clone()
                );
        return config.stepFunction().apply(frame);
    }

    record FrameResult(boolean advance, boolean cancel) {
    }
}
