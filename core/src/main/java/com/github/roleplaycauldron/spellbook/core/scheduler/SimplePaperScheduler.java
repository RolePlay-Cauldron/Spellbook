package com.github.roleplaycauldron.spellbook.core.scheduler;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * A simple implementation of the {@link SimpleScheduler} interface for the Bukkit API.
 * This scheduler allows asynchronous execution of tasks using the Bukkit scheduler.
 */
public class SimplePaperScheduler implements SimpleScheduler {

    private final JavaPlugin plugin;

    private final BukkitScheduler bukkitScheduler;

    /**
     * Create a new {@link SimplePaperScheduler}
     *
     * @param plugin          the Main class of the Plugin using this Scheduler
     * @param bukkitScheduler the Bukkit Scheduler used for running Tasks
     */
    public SimplePaperScheduler(JavaPlugin plugin, BukkitScheduler bukkitScheduler) {
        this.plugin = plugin;
        this.bukkitScheduler = bukkitScheduler;
    }

    @Override
    public <T> CompletableFuture<T> runTaskAsync(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        bukkitScheduler.runTaskAsynchronously(plugin, () -> {
            try {
                T result = callable.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> runTaskAsync(Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        bukkitScheduler.runTaskAsynchronously(plugin, () -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
