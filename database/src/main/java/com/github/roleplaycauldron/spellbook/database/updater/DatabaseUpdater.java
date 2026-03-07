package com.github.roleplaycauldron.spellbook.database.updater;

import com.github.roleplaycauldron.spellbook.core.logger.WrappedLogger;
import com.github.roleplaycauldron.spellbook.database.ConnectionProvider;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * This class will perform Database Updates based on the Versions provided in a VersionRepository.
 */
public class DatabaseUpdater {

    /**
     * Logger used by this class
     */
    private WrappedLogger log;

    /**
     * Connection provider for database connections
     */
    private ConnectionProvider connectionProvider;

    /**
     * Version repository containing and managing all versions
     */
    private VersionRepositoryBase versionRepository;

    /**
     * Record containing information about the version table
     */
    private VersionTableInformation versionTableInformation;

    /**
     * Only the Builder can create instances without providing all required fields
     */
    private DatabaseUpdater() {}

    /**
     * Subclasses need to provide all fields to the constructor.
     * Instead of a public constructor this classes {@link Builder} should be used.
     *
     * @param log                     the logger
     * @param connectionProvider      the connection provider implemented by the plugin
     * @param versionRepository       the plugins version repository, pre-filled with versions
     * @param versionTableInformation record containing information about the version table
     */
    protected DatabaseUpdater(WrappedLogger log,
                              ConnectionProvider connectionProvider,
                              VersionRepositoryBase versionRepository,
                              VersionTableInformation versionTableInformation) {
        this.log = log;
        this.connectionProvider = connectionProvider;
        this.versionRepository = versionRepository;
        this.versionTableInformation = versionTableInformation;
    }

    /**
     * Create a new DatabaseUpdater instance using the Builder pattern
     *
     * @return a Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Check for the current version in the version table and apply all outstanding migrations.</p>
     * <p>If no version table exists, the Updater will run all migrations in order.</p>
     * <p>If you provide special First Startup Queries, call {@link #firstStartup()} on your plugins first start</p>
     *
     * @return true if the database is up to date after running all migrations, false if the migrations fail gracefully
     * @throws DatabaseUpdateException if an error occurs during the update process or the migrations fail less gracefully
     */
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

    /**
     * <p>Run this if your Plugin starts for the first time.</p>
     * <p>The Updater will apply the versions provided by the provided
     * VersionRepository's {@link VersionRepositoryBase#getVersionsForFirstStartup()} method.</p>
     * <p>If there are no Versions with special firstStartup Queries the Updater will fall back to the default update process through {@link #checkAndApplyUpdates()}.</p>
     * <p>If the Updater detects that the Database is already initialized, it will run the default update process through {@link #checkAndApplyUpdates()} instead.</p>
     *
     * @return true if the database is up to date after running all migrations, false if the migrations fail gracefully
     * @throws DatabaseUpdateException if an error occurs during the update process or the migrations fail less gracefully
     */
    public boolean firstStartup() throws DatabaseUpdateException {
        var firstStartupQueries = versionRepository.getVersionsForFirstStartup();
        if (firstStartupQueries.isEmpty()) {
            log.info("No version with FirstStartup Queries exists. Running default update process");
            return checkAndApplyUpdates();
        }
        if (getCurrentVersion() != 0) {
            log.info("FirstStartup called, but the Database is already initialized. Running default update process instead");
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

            for (var cQueries : dbVer.conditionalUpgradeQueries()) {
                if (StringUtils.isNotBlank(cQueries.conditionQuery())
                        && StringUtils.isNotBlank(cQueries.expectedResult())) {

                    try (PreparedStatement preparedStatement = connection.prepareStatement(cQueries.conditionQuery())) {
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (!resultSet.next() || !cQueries.expectedResult().equals(resultSet.getString(1))) {
                            continue;
                        }
                    } catch (SQLException e) {
                        connection.rollback();
                        throw new DatabaseUpdateException("Error checking condition for version %d".formatted(dbVer.versionNumber()), e);
                    }
                }

                for (String statement : cQueries.queries()) {
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

    /**
     * A Builder class for {@link DatabaseUpdater}. Create using {@link DatabaseUpdater#builder()}
     */
    public static class Builder {

        /**
         * Instance created by this Builder
         */
        private final DatabaseUpdater instance;

        /**
         * Can only be created by {@link DatabaseUpdater#builder()}
         */
        private Builder() {
            this.instance = new DatabaseUpdater();
        }

        /**
         * Provide a logger to the DatabaseUpdater. It will be used to log progress and errors.
         *
         * @param log the logger to use
         * @return this Builder
         */
        public Builder logger(WrappedLogger log) {
            this.instance.log = log;
            return this;
        }

        /**
         * Provide a connection provider to the DatabaseUpdater. It will be used to get database connections.
         *
         * @param connectionProvider the connection provider
         * @return this Builder
         */
        public Builder connectionProvider(ConnectionProvider connectionProvider) {
            this.instance.connectionProvider = connectionProvider;
            return this;
        }

        /**
         * Provide a VersionRepository to the DatabaseUpdater. It will be used to get database versions.
         *
         * @param versionRepository the VersionRepository
         * @return this Builder
         */
        public Builder versionRepository(VersionRepositoryBase versionRepository) {
            this.instance.versionRepository = versionRepository;
            return this;
        }

        /**
         * Provide information about the version table. This information may not change!
         *
         * @param tableName                name of the version table
         * @param getMaxVersionQuery       SQL query to get the current version. It must be the first result of the query.
         * @param recordVersionUpdateQuery SQL query to update the version. It must have one placeholder (question mark). The placeholder will be replaced with the new version.
         * @return this Builder
         */
        public Builder versionTable(String tableName,
                                    String getMaxVersionQuery,
                                    String recordVersionUpdateQuery) {
            this.instance.versionTableInformation = new VersionTableInformation(
                    tableName,
                    getMaxVersionQuery,
                    recordVersionUpdateQuery
            );
            return this;
        }

        /**
         * Build the DatabaseUpdater instance.
         *
         * @return the DatabaseUpdater instance
         * @throws IllegalStateException if required fields are missing
         */
        public DatabaseUpdater build() throws IllegalStateException {
            try {
                Objects.requireNonNull(this.instance.log, "logger");
                Objects.requireNonNull(this.instance.connectionProvider, "ConnectionProvider");
                Objects.requireNonNull(this.instance.versionRepository, "VersionRepository");
                Objects.requireNonNull(this.instance.versionTableInformation, "VersionTableInformation");
                Objects.requireNonNull(this.instance.versionTableInformation.tableName(),
                        "VersionTableInformation.tableName");
                Objects.requireNonNull(this.instance.versionTableInformation.getMaxVersionQuery(),
                        "VersionTableInformation.getMaxVersionQuery");
                Objects.requireNonNull(this.instance.versionTableInformation.recordVersionUpdateQuery(),
                        "VersionTableInformation.recordVersionUpdateQuery");
            } catch (NullPointerException e) {
                throw new IllegalStateException("The following fields are missing %s".formatted(e.getMessage()), e);
            }
            return this.instance;
        }
    }
}
