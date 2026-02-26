package com.github.roleplaycauldron.spellbook.database.updater;

import com.github.roleplaycauldron.spellbook.core.logger.WrappedLogger;
import com.github.roleplaycauldron.spellbook.database.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This Class lets you perform automated Database Updates
 * using Versions defined by the {@link DatabaseVersion} interface
 * collected in a {@link VersionRepository}
 */
public class DatabaseUpdater {

    private final WrappedLogger log;

    private final ConnectionProvider connectionProvider;

    private final VersionRepository versionRepository;

    private final String versionTableName;

    /**
     * Creates a new {@link DatabaseUpdater}
     *
     * @param log a Logger to inform about the Updaters Actions
     * @param connectionProvider the {@link ConnectionProvider} providing the Database Connections
     * @param versionRepository the {@link VersionRepository} containing all applicable {@link DatabaseVersion}s
     * @param versionTableName the name of the table that will track the version changes.
     *                         You should give this a unique name and not rename it.
     *                         This table is used to track which version the database currently conforms to.
     *                         A good example might be 'myplugin_schema-version'
     */
    public DatabaseUpdater(WrappedLogger log, ConnectionProvider connectionProvider,
                           VersionRepository versionRepository, String versionTableName) {
        this.log = log;
        this.connectionProvider = connectionProvider;
        this.versionRepository = versionRepository;
        this.versionTableName = versionTableName;
    }

    /**
     * Calling this method will perform all outstanding Database Migrations
     *
     * @return whether the Migrations were successful
     * @throws DatabaseUpdateException thrown if a hard error occurs
     */
    public boolean checkAndApplyUpdates() throws DatabaseUpdateException {
        int currVer = getCurrentVersion();
        int maxVer = versionRepository.getMaxVersion();
        if (currVer >= maxVer) {
            log.infoF("Database is up to date (current: %d, max: %d). Nothing to update.", currVer, maxVer);
            return true;
        } else {
            log.infoF("Database is out of date (current: %d, max: %d). Starting upgrade process...", currVer, maxVer);
        }
        List<DatabaseVersion> missingUpgrades = versionRepository.getVersions(currVer);
        for (DatabaseVersion version : missingUpgrades) {
            log.infoF("Updating from version %d to version %d.", currVer, version.getVersionNumber());
            boolean success = performVersionUpdate(version);
            if (success) {
                int newCurrVer = getCurrentVersion();
                log.infoF("Successfully updated version %d to version %d. Version is now %d", currVer, version.getVersionNumber(), newCurrVer);
                currVer = newCurrVer;
            } else {
                log.errorF("Failed to update from version %d to version %d. Aborting all further upgrades.", currVer, version.getVersionNumber());
                return false;
            }
        }
        return true;
    }

    private boolean performVersionUpdate(DatabaseVersion dbVer) {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            for (String statement : dbVer.getUpgradeQueries()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    connection.rollback();
                    log.errorF("Error updating to Version %d: %s", dbVer.getVersionNumber(), e.getMessage());
                    return false;
                }
            }

            String protocolSql = "INSERT INTO " + versionTableName + " (version_no) VALUES (?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(protocolSql)) {
                preparedStatement.setInt(1, dbVer.getVersionNumber());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                log.errorF("Error inserting the upgrade protocol for version %d: %s", dbVer.getVersionNumber(), e.getMessage());
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            log.errorF("Error updating to Version %d: %s", dbVer.getVersionNumber(), e.getMessage());
            return false;
        }
    }

    private int getCurrentVersion() throws DatabaseUpdateException {
        try (Connection connection = connectionProvider.getConnection()) {
            try (ResultSet tables = connection.getMetaData().getTables(null, null, versionTableName, null)) {
                if (!tables.next()) {
                    return 0;
                }
            }

            String sql = "SELECT MAX(t.version_no) as curr_ver FROM " + versionTableName + " t";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("curr_ver");
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseUpdateException("Error getting current version", e);
        }
    }
}
