package com.github.roleplaycauldron.spellbook.effect.executor;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
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

            @Override
            public void run() {
                if (config.maxRuns() != -1 && runIndex >= config.maxRuns()) {
                    cancel();
                    return;
                }

                Location origin = config.originAnchor().resolve();

                if (origin == null || origin.getWorld() == null) {
                    if (config.cancelIfOriginUnavailable()) {
                        cancel();
                    }
                    return;
                }

                Location target = null;
                if (config.targetAnchor() != null) {
                    target = config.targetAnchor().resolve();

                    if (target == null || target.getWorld() == null) {
                        if (config.cancelIfTargetUnavailable()) {
                            cancel();
                        }
                        return;
                    }

                    if (!origin.getWorld().equals(target.getWorld())) {
                        if (config.cancelIfWorldsDiffer()) {
                            cancel();
                        }
                        return;
                    }
                }

                Collection<? extends Player> viewers = config.viewerSource().resolveViewers();

                long elapsedTicks = runIndex * config.periodTicks();
                double elapsedSeconds = elapsedTicks / 20.0;

                EffectExecutionConfig.ExecutionFrame frame =
                        new EffectExecutionConfig.ExecutionFrame(
                                runIndex,
                                elapsedTicks,
                                elapsedSeconds,
                                origin.clone(),
                                target == null ? null : target.clone()
                        );

                int step = config.stepFunction().apply(frame);

                EffectContext context = new EffectContext(
                        origin.getWorld(),
                        origin.clone(),
                        target == null ? null : target.clone(),
                        viewers,
                        step,
                        elapsedTicks,
                        elapsedSeconds
                );

                effect.render(context);
                runIndex++;
            }
        };

        BukkitTask task = runnable.runTaskTimer(
                plugin,
                config.delayTicks(),
                config.periodTicks()
        );

        return new RunningEffect(task);
    }
}