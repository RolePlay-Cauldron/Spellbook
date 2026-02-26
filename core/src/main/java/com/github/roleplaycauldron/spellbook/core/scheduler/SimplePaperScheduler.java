package com.github.roleplaycauldron.spellbook.core.scheduler;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * An easy-to-use Scheduler that makes running Tasks asynchronously a breeze!
 */
public class SimplePaperScheduler implements SimpleScheduler {

    private final JavaPlugin plugin;

    /**
     * Create a new {@link SimplePaperScheduler}
     *
     * @param plugin the Main class of the Plugin using this Scheduler
     */
    public SimplePaperScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
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

    @Override
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
