package com.github.roleplaycauldron.spellbook.database.updater;

public record VersionTableInformation(
        String tableName,
        String getMaxVersionQuery,
        String recordVersionUpdateQuery
) {
}
