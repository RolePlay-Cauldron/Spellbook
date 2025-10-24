package com.github.roleplaycauldron.spellbook.core;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * An easy-to-use Scheduler that makes running Tasks asynchronously a breeze!
 */
public class SimpleScheduler {
    private final JavaPlugin plugin;

    /**
     * Create a new {@link SimpleScheduler}
     *
     * @param plugin the Main class of the Plugin using this Scheduler
     */
    public SimpleScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs the given callable asynchronously and returns a CompletableFuture
     * that will be completed with the result or exceptionally if an error occurs.
     *
     * @param callable the Task to asynchronously execute
     * @return A {@link CompletableFuture} of the Tasks return value
     * @param <T> the Tasks return values type
     */
    public <T> CompletableFuture<T> runTaskAsync(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                T result = callable.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Runs the given runnable asynchronously and returns a CompletableFuture
     * that will be completed when the task finishes.
     *
     * @param runnable the Task to asynchronously execute
     * @return A {@link CompletableFuture} without a return value
     */
    public CompletableFuture<Void> runTaskAsync(Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
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
