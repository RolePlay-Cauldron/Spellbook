package com.github.roleplaycauldron.spellbook.core.config;

import com.github.roleplaycauldron.spellbook.core.WrappedLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * This Utility lets you update a {@link YamlConfiguration} using the following steps:<br>
 * 1. Copy the old config to a .bak file<br>
 * 2. Copy the new original config (e.g. from plugin jar) to the desired Location<br>
 * 3. Apply the old Configuration Values to the newly overwritten configuration<br>
 * <br>
 * <b>Advantages:</b><br>
 * - Updates the comments according to your new original configuration<br>
 * <b>Disadvantages:</b><br>
 * - Does not preserve users' changes to the config apart from the values (= removes user-made comments)<br>
 * - Might not work with special YAML Structures
 */
public class ReplaceAndMergeConfigUpdater {

    private final WrappedLogger log;
    private final JavaPlugin plugin;

    /**
     * Create a Configuration Updater for the given Plugin
     *
     * @param log the Logger
     * @param plugin the Plugin
     */
    public ReplaceAndMergeConfigUpdater(WrappedLogger log, JavaPlugin plugin) {
        this.log = log;
        this.plugin = plugin;
    }

    /**
     * Performs a Configuration Update on the specified file.
     * See {@link ReplaceAndMergeConfigUpdater} for strategy documentation.
     *
     * @param userFile    the users' configuration. Path needs to be relative to the plugins data folder<br>
     *                    See: {@link JavaPlugin#getDataFolder()}
     * @param defaultFile the plugins default configuration. Path needs to be relative to the plugins resource folder<br>
     *                    See: {@link JavaPlugin#getResource(String)}
     *                    /{@link JavaPlugin#saveResource(String, boolean)}
     * @return true if the migration was successful, false otherwise
     * @see ReplaceAndMergeConfigUpdater
     */
    public boolean updateConfig(String userFile, String defaultFile) {
        // TODO: Logging, adjustable LogLevel?, .bak optional, .bak delete/keep option
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new ConfigurationMigrationException(
                    "Could not create plugin data folder: %s", dataFolder.getAbsolutePath());
        }

        File targetFile = new File(dataFolder, userFile);
        File backupFile = new File(dataFolder, defaultFile + ".bak");

        try {
            if (backupFile.exists() && !backupFile.delete()) {
                throw new ConfigurationMigrationException(
                        "Could not delete existing backup file: %s", backupFile.getAbsolutePath());
            }

            if (targetFile.exists() && !targetFile.renameTo(backupFile)) {
                throw new ConfigurationMigrationException(
                        "Could not rename old config to backup: %s", targetFile.getAbsolutePath());
            }

            plugin.saveResource(defaultFile, true);

            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(targetFile);
            YamlConfiguration oldConfig = backupFile.exists()
                    ? YamlConfiguration.loadConfiguration(backupFile)
                    : new YamlConfiguration();

            for (String key : oldConfig.getKeys(true)) {
                Object value = oldConfig.get(key);
                newConfig.set(key, value);
            }

            newConfig.save(targetFile);

            plugin.reloadConfig();
            return true;
        } catch (Exception e) {
            log.errorF("Error while migrating config: ", e);
            try {
                if (!targetFile.exists() && backupFile.exists()) {
                    log.infoF("Restoring original config from .bak file...");
                    if (!backupFile.renameTo(targetFile)) {
                        log.errorF("Failed to restore original config from .bak file");
                    }
                }
            } catch (Exception restoreEx) {
                log.errorF("Error while attempting to restore original config from .bak file: %s", restoreEx);
            }
            // TODO make this optional
            throw new ConfigurationMigrationException("Config migration failed: %s".formatted(e.getMessage()), e);
        }
//        return false;
    }
}
