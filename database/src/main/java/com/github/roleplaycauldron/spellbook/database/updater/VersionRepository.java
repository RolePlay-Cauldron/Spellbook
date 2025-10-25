package com.github.roleplaycauldron.spellbook.database.updater;

import java.util.List;

/**
 * A Class that contains all Database Versions of a Plugin
 */
public abstract class VersionRepository {

    /**
     * Constructs a new instance of the VersionRepository
     */
    public VersionRepository() {}

    /**
     * This method provides all existing Database Versions
     *
     * @return A List of DatabaseVersions
     */
    public abstract List<DatabaseVersion> getAllVersions();

    /**
     * Get the highest Database Version Number this Repository contains
     *
     * @return the highest version number
     */
    public int getMaxVersion() {
        return getAllVersions().stream().mapToInt(DatabaseVersion::getVersionNumber).max().orElse(0);
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
        return getAllVersions().stream().filter(dbVer -> dbVer.getVersionNumber() > lowerBoundExclusive).toList();
    }
}
