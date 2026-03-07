package com.github.roleplaycauldron.spellbook.database.updater;

import java.util.List;

/**
 * A record containing database version information.
 *
 * @param versionNumber             the version number of the database
 * @param conditionalUpgradeQueries the list of conditional upgrade queries (also contains the unconditional queries)
 * @param firstStartupQueries       the list of queries to run on the first startup
 */
public record DatabaseVersion(
        int versionNumber,
        List<ConditionalUpgradeQuery> conditionalUpgradeQueries,
        List<String> firstStartupQueries
) {
}
