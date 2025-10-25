package com.github.roleplaycauldron.spellbook.database.updater;

import java.util.List;

/**
 * The general contract for all Database Versions
 */
public interface DatabaseVersion {

    /**
     * Returns the Version Number of this Database Version
     *
     * @return the Version Number
     */
    int getVersionNumber();

    /**
     * Returns all Queries that will need to be executed to upgrade from the previous to this version
     *
     * @return a List of Queries that will update the previous to this version
     */
    List<String> getUpgradeQueries();
}
