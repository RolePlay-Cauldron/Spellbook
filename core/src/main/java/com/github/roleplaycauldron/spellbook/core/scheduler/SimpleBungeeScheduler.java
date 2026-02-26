package com.github.roleplaycauldron.spellbook.core.scheduler;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * A simple implementation of the {@link SimpleScheduler} interface
 * for environments using BungeeCord.
 */
public class SimpleBungeeScheduler implements SimpleScheduler {

    private final Plugin plugin;

    private final TaskScheduler scheduler;

    /**
     * Constructs a new {@code SimpleBungeeScheduler}.
     *
     * @param plugin    the plugin instance that will be associated with this scheduler
     * @param scheduler the underlying {@link TaskScheduler} used for task execution
     */
    public SimpleBungeeScheduler(Plugin plugin, TaskScheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    @Override
    public <T> CompletableFuture<T> runTaskAsync(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        ScheduledTask task = scheduler.schedule(plugin, () -> {
            try {
                future.complete(callable.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }, 0, java.util.concurrent.TimeUnit.MILLISECONDS);

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
        ScheduledTask task = scheduler.schedule(plugin, () -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }, 0, java.util.concurrent.TimeUnit.MILLISECONDS);

        future.whenComplete((r, t) -> {
            if (future.isCancelled()) {
                task.cancel();
            }
        });
        return future;
    }
}