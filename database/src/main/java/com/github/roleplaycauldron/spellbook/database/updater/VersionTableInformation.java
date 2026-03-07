package com.github.roleplaycauldron.spellbook.database.updater;

/**
 * A record containing information about a database version table
 *
 * @param tableName                name of the version table
 * @param getMaxVersionQuery       SQL query to get the current version. It must be the first result of the query.
 * @param recordVersionUpdateQuery SQL query to update the version. It must have one placeholder (question mark). The placeholder will be replaced with the new version.
 */
public record VersionTableInformation(
        String tableName,
        String getMaxVersionQuery,
        String recordVersionUpdateQuery
) {
}
