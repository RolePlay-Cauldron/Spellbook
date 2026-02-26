package com.github.roleplaycauldron.spellbook.core.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Defines an interface for a simple scheduler that allows executing tasks asynchronously.
 * This interface provides methods to run both {@link Callable} and {@link Runnable} tasks,
 * returning a {@link CompletableFuture} to handle the results or exceptions.
 * <p>
 * There's no need to create an own implementation of this interface. You can use {@link SimplePaperScheduler},
 * {@link SimpleVelocityScheduler} or {@link SimpleBungeeScheduler} instead.
 */
public interface SimpleScheduler {

    /**
     * Runs the given callable asynchronously and returns a CompletableFuture
     * that will be completed with the result or exceptionally if an error occurs.
     *
     * @param callable the Task to asynchronously execute
     * @param <T>      the Tasks return values type
     * @return A {@link CompletableFuture} of the Tasks return value
     */
    <T> CompletableFuture<T> runTaskAsync(Callable<T> callable);

    /**
     * Runs the given runnable asynchronously and returns a CompletableFuture
     * that will be completed when the task finishes.
     *
     * @param runnable the Task to asynchronously execute
     * @return A {@link CompletableFuture} without a return value
     */
    CompletableFuture<Void> runTaskAsync(Runnable runnable);
}
