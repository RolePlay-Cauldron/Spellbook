package com.github.roleplaycauldron.spellbook.core.scheduler;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * An easy-to-use Scheduler that makes running Tasks asynchronously a breeze!
 */
public class SimpleBungeeScheduler implements SimpleScheduler {

    private final Plugin plugin;

    /**
     * Constructs a new {@code SimpleBungeeScheduler}.
     *
     * @param plugin the plugin instance that will be associated with this scheduler
     */
    public SimpleBungeeScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <T> CompletableFuture<T> runTaskAsync(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        ScheduledTask task = plugin.getProxy().getScheduler().schedule(plugin, () -> {
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
        ScheduledTask task = plugin.getProxy().getScheduler().schedule(plugin, () -> {
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