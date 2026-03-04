package com.github.roleplaycauldron.spellbook.database.updater2;

import java.util.*;
import java.util.function.*;

import com.github.roleplaycauldron.spellbook.database.updater2.builder.*;

public abstract class VersionRepositoryBase {

    private final List<DatabaseVersion> versions;

    protected VersionRepositoryBase(List<DatabaseVersion> versions) {
        this.versions = versions.stream()
                .sorted(Comparator.comparingInt(DatabaseVersion::versionNumber))
                .toList();
    }

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

    public List<DatabaseVersion> getAllVersions() {
        return versions;
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
        var highestWithStartup = versions.stream()
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

    public static VersionListBuilder builder() {
        return new VersionListBuilder();
    }
}
