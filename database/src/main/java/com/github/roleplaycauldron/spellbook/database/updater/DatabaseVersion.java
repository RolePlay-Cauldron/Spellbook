package com.github.roleplaycauldron.spellbook.database.updater;

import java.util.List;

public record DatabaseVersion(
        int versionNumber,
        List<ConditionalUpgradeQuery> conditionalUpgradeQueries,
        List<String> firstStartupQueries
) {
}
