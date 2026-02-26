package com.github.roleplaycauldron.spellbook.core.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * A simple implementation of the {@link SimpleScheduler} interface for Velocity,
 * facilitating the asynchronous execution of tasks using a Velocity {@link Scheduler}.
 */
public class SimpleVelocityScheduler implements SimpleScheduler {

    private final Object plugin;

    private final Scheduler scheduler;

    /**
     * Constructs a new {@code SimpleVelocityScheduler}.
     *
     * @param plugin    the plugin instance using this scheduler
     * @param scheduler the underlying {@link Scheduler} used for task execution
     */
    public SimpleVelocityScheduler(final Object plugin, final Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    @Override
    public <T> CompletableFuture<T> runTaskAsync(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        final ScheduledTask task = scheduler
                .buildTask(plugin, () -> {
                    try {
                        T result = callable.call();
                        future.complete(result);
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                }).schedule();

        future.whenComplete((r, t) -> {
            if (future.isCancelled()) {
                task.cancel();
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> runTaskAsync(Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        final ScheduledTask task = scheduler
                .buildTask(plugin, () -> {
                    try {
                        runnable.run();
                        future.complete(null);
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                })
                .schedule();

        future.whenComplete((r, t) -> {
            if (future.isCancelled()) {
                task.cancel();
            }
        });
        return future;
    }
}