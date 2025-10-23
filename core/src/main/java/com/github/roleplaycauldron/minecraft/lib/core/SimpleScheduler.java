package com.github.roleplaycauldron.minecraft.lib.core;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class SimpleScheduler {
    private final JavaPlugin plugin;

    public SimpleScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs the given callable asynchronously and returns a CompletableFuture
     * that will be completed with the result or exceptionally if an error occurs.
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

