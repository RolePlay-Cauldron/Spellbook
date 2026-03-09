package com.github.roleplaycauldron.spellbook.database.updater;

import com.github.roleplaycauldron.spellbook.database.updater.builder.VersionListBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;

/**
 * <p>Base class for managing database versions with default functionality.
 * This class will provide the versions to a {@link DatabaseUpdater}.</p>
 * <p>See {@link DefaultVersionRepository} to use this implementation without extending it.</p>
 */
public abstract class VersionRepositoryBase {

    /**
     * List of database versions managed by this repository.
     */
    private final List<DatabaseVersion> versions;

    /**
     * Creates a new Version Repository
     *
     * @param versions the database versions this repository will manage
     */
    protected VersionRepositoryBase(List<DatabaseVersion> versions) {
        this.versions = versions.stream()
                .sorted(Comparator.comparingInt(DatabaseVersion::versionNumber))
                .toList();
    }

    /**
     * Gets the latest database version (highest version number)
     *
     * @return the latest version
     */
    public DatabaseVersion getLatestVersion() {
        return versions.getLast();
    }

    /**
     * Gets all Versions higher than the given Version Number. The check is exclusive.<br>
     * <br>
     * Example: getVersions(2) == [Version3, Version4]
     *
     * @param lowerBoundExclusive the Version to count up from
     * @return all Database Versions higher than the given one
     */
    public List<DatabaseVersion> getVersions(int lowerBoundExclusive) {
        return versions.stream()
                .filter(version -> version.versionNumber() > lowerBoundExclusive)
                .toList();
    }

    /**
     * Returns a copied list of all versions managed by this repository
     *
     * @return all versions
     */
    public List<DatabaseVersion> getAllVersions() {
        return new ArrayList<>(versions);
    }

    /**
     * <p>Retrieves a list of {@code DatabaseVersion} objects for use during the first startup.</p>
     * <p>This method identifies the highest version containing non-empty startup queries and
     * filters the versions to include only those with version numbers greater than or equal
     * to that identified version. If no versions contain startup queries, all versions are returned.</p>
     *
     * @return a list of {@code DatabaseVersion} objects determined based on their startup query availability
     */
    public List<DatabaseVersion> getVersionsForFirstStartup() {
        OptionalInt highestWithStartup = versions.stream()
                .filter(Predicate.not(version -> version.firstStartupQueries().isEmpty()))
                .mapToInt(DatabaseVersion::versionNumber)
                .max();

        if (highestWithStartup.isEmpty()) {
            return versions;
        } else {
            return versions.stream()
                    .filter(version -> version.versionNumber() >= highestWithStartup.getAsInt())
                    .toList();
        }
    }

    /**
     * Create a new Builder for Database Versions
     *
     * @return a new VersionListBuilder
     */
    public static VersionListBuilder builder() {
        return new VersionListBuilder();
    }
}
