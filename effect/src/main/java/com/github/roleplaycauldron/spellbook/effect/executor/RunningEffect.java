package com.github.roleplaycauldron.spellbook.effect.executor;

import org.bukkit.scheduler.BukkitTask;

/**
 * Represents an ongoing effect execution.
 */
public final class RunningEffect {

    private final BukkitTask task;

    /**
     * Creates a new RunningEffect
     *
     * @param task the BukkitTask representing the ongoing effect
     */
    public RunningEffect(BukkitTask task) {
        this.task = task;
    }

    /**
     * Cancels the ongoing effect execution.
     */
    public void cancel() {
        task.cancel();
    }

    /**
     * Checks if the effect execution has been canceled.
     *
     * @return {@code true} if the effect execution has been canceled, {@code false} otherwise
     */
    public boolean isCancelled() {
        return task.isCancelled();
    }

    /**
     * Returns the task ID of the {@link BukkitTask} associated with this effect.
     *
     * @return the task ID
     */
    public int getTaskId() {
        return task.getTaskId();
    }
}