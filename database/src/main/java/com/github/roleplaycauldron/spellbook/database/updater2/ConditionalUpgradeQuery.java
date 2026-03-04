package com.github.roleplaycauldron.spellbook.database.updater2;

import java.util.List;

public record ConditionalUpgradeQuery(
        String conditionQuery,
        String expectedResult,
        List<String> queries
) {
}
