package com.github.roleplaycauldron.spellbook.database.updater2;

import java.util.*;

public record DatabaseVersion(
        int versionNumber,
        List<ConditionalUpgradeQuery> conditionalUpgradeQueries,
        List<String> firstStartupQueries
) {
}
