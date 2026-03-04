package com.github.roleplaycauldron.spellbook.database.updater2;

public record VersionTableInformation(
        String tableName,
        String getMaxVersionQuery,
        String recordVersionUpdateQuery
) {
}
