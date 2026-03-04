package com.github.roleplaycauldron.spellbook.database.updater2;

import com.github.roleplaycauldron.spellbook.core.logger.WrappedLogger;
import com.github.roleplaycauldron.spellbook.database.ConnectionProvider;
import com.github.roleplaycauldron.spellbook.database.updater.DatabaseUpdateException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseUpdater {

    private final WrappedLogger log;

    private final ConnectionProvider connectionProvider;

    private final VersionRepositoryBase versionRepository;

    private final VersionTableInformation versionTableInformation;

    // TODO Builder
    public DatabaseUpdater(WrappedLogger log,
                           ConnectionProvider connectionProvider,
                           VersionRepositoryBase versionRepository,
                           VersionTableInformation versionTableInformation) {
        this.log = log;
        this.connectionProvider = connectionProvider;
        this.versionRepository = versionRepository;
        this.versionTableInformation = versionTableInformation;
    }

    public boolean checkAndApplyUpdates() throws DatabaseUpdateException {
        int currVer = getCurrentVersion();
        int maxVer = versionRepository.getLatestVersion().versionNumber();
        if (currVer >= maxVer) {
            log.infoF("Database is up to date (current: %d, max: %d). Nothing to update.", currVer, maxVer);
            return true;
        } else {
            log.infoF("Database is out of date (current: %d, max: %d). Starting upgrade process...", currVer, maxVer);
        }
        return performMigrationForVersions(versionRepository.getVersions(currVer), currVer);
    }

    public boolean firstStartup() throws DatabaseUpdateException {
        var firstStartupQueries = versionRepository.getVersionsForFirstStartup();
        if (firstStartupQueries.isEmpty()) {
            return checkAndApplyUpdates();
        }

        var firstStartup = firstStartupQueries.getFirst();
        try (Connection connection = connectionProvider.getConnection()) {
            log.infoF("Performing first startup queries for database version %d", firstStartup.versionNumber());
            for (String statement : firstStartup.firstStartupQueries()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseUpdateException("Error performing first startup queries for version %d".formatted(firstStartup.versionNumber()), e);
        }

        performMigrationForVersions(firstStartupQueries.subList(1, firstStartupQueries.size()), firstStartup.versionNumber());
        return true;
    }

    private boolean performMigrationForVersions(List<DatabaseVersion> missingUpgrades, int currVer) {
        for (DatabaseVersion version : missingUpgrades) {
            log.infoF("Updating from version %d to version %d.", currVer, version.versionNumber());
            boolean success = performVersionUpdate(version);
            if (success) {
                int newCurrVer = getCurrentVersion();
                log.infoF("Successfully updated version %d to version %d. Version is now %d", currVer, version.versionNumber(), newCurrVer);
                currVer = newCurrVer;
            } else {
                log.errorF("Failed to update from version %d to version %d. Aborting all further upgrades.", currVer, version.versionNumber());
                return false;
            }
        }
        return true;
    }

    private boolean performVersionUpdate(DatabaseVersion dbVer) throws DatabaseUpdateException {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            // TODO unconditional Queries?

            for (var conditionalQuerySet : dbVer.conditionalUpgradeQueries()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(conditionalQuerySet.conditionQuery())) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (!resultSet.next() || !conditionalQuerySet.expectedResult().equals(resultSet.getString(1))) {
                        continue;
                    }
                }

                for (String statement : conditionalQuerySet.queries()) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        connection.rollback();
                        throw new DatabaseUpdateException("Error updating to Version %d".formatted(dbVer.versionNumber()), e);
                    }
                }
            }
            updateVersionIndex(connection, dbVer.versionNumber());
            connection.commit();
            return true;
        } catch (SQLException e) {
            throw new DatabaseUpdateException("Error updating to Version %d".formatted(dbVer.versionNumber()), e);
        }
    }

    private void updateVersionIndex(Connection connection, int versionNumber) throws DatabaseUpdateException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(versionTableInformation.recordVersionUpdateQuery())) {
            preparedStatement.setInt(1, versionNumber);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            throw new DatabaseUpdateException("Error inserting the upgrade protocol for version %d".formatted(versionNumber), e);
        }
    }

    private int getCurrentVersion() throws DatabaseUpdateException {
        try (Connection connection = connectionProvider.getConnection()) {
            try (ResultSet tables = connection.getMetaData().getTables(null, null, versionTableInformation.tableName(), null)) {
                if (!tables.next()) {
                    return 0;
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(versionTableInformation.getMaxVersionQuery())) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseUpdateException("Error getting current version", e);
        }
    }
}
