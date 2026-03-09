package com.github.roleplaycauldron.spellbook.database.updater;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * A record containing conditional upgrade queries.
 * If conditionQuery or expectedResult are null/empty, it counts as unconditional.
 *
 * @param priority the priority of the query
 * @param conditionQuery the condition query
 * @param expectedResult the expected result of the condition query
 * @param queries the queries to run if the condition query is fulfilled
 */
public record ConditionalUpgradeQuery(
        int priority,
        String conditionQuery,
        String expectedResult,
        List<String> queries
) {
    /**
     * Checks if this query is unconditional (conditionQuery or expectedResult are null/empty)
     * @return true if unconditional, false otherwise
     */
    public boolean isUnconditional() {
        return StringUtils.isBlank(conditionQuery) || StringUtils.isBlank(expectedResult);
    }
}
