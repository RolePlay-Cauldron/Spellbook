package com.github.roleplaycauldron.spellbook.core.scheduler;

import com.velocitypowered.api.scheduler.Scheduler;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Factory class for creating instances of {@link SimpleScheduler} implementations.
 */
public class SimpleSchedulerFactory {

    /**
     * Constructs a new {@code SimpleSchedulerFactory}.
     */
    public SimpleSchedulerFactory() {
        // Empty
    }

    /**
     * Creates an instance of {@link SimplePaperScheduler} using the provided plugin.
     *
     * @param paperPlugin the plugin instance to be associated with the created scheduler
     * @return an instance of {@link SimplePaperScheduler} configured with the provided plugin
     */
    public SimpleScheduler create(JavaPlugin paperPlugin) {
        return new SimplePaperScheduler(paperPlugin);
    }

    /**
     * Creates a new instance of {@link SimpleBungeeScheduler} with the specified plugin.
     *
     * @param bungeePlugin the plugin instance to be associated with the created scheduler
     * @return an instance of {@link SimpleBungeeScheduler} configured with the provided plugin
     */
    public SimpleScheduler create(Plugin bungeePlugin) {
        return new SimpleBungeeScheduler(bungeePlugin);
    }

    /**
     * Creates a new instance of {@link SimpleVelocityScheduler} using the given plugin and scheduler.
     *
     * @param plugin    the plugin object that will be associated with the scheduler
     * @param scheduler the scheduler implementation used for task execution
     * @return an instance of {@link SimpleVelocityScheduler} configured with the provided plugin and scheduler
     */
    public SimpleScheduler create(Object plugin, Scheduler scheduler) {
        return new SimpleVelocityScheduler(plugin, scheduler);
    }
}
