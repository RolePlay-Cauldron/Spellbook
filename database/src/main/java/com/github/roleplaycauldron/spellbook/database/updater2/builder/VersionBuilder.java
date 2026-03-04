package com.github.roleplaycauldron.spellbook.database.updater2.builder;

import java.util.*;

import com.github.roleplaycauldron.spellbook.database.updater2.*;

public class VersionBuilder {

    private final VersionListBuilder parentList;

    private final int versionNumber;

    private final List<ConditionalUpgradeQuery> conditionalUpgradeQueries = new LinkedList<>();

    private final List<String> firstStartupQueries = new LinkedList<>();

    public VersionBuilder(int versionNumber, VersionListBuilder parent) {
        this.versionNumber = versionNumber;
        this.parentList = parent;
    }

    public VersionBuilder addFirstStartupQuery(String query) {
        firstStartupQueries.add(query);
        return this;
    }

    public ConditionalQueryBuilder addConditionalQuery(String conditionQuery, String expectedResult) {
        return new ConditionalQueryBuilder(this, conditionQuery, expectedResult);
    }

    public VersionListBuilder next() {
        parentList.version(new DatabaseVersion(versionNumber, conditionalUpgradeQueries, firstStartupQueries));
        return parentList;
    }

    // TODO easy method for unconditional queries

    private void addConditionalUpgradeQuery(ConditionalUpgradeQuery query) {
        conditionalUpgradeQueries.add(query);
    }

    public static class ConditionalQueryBuilder {

        private final VersionBuilder parentVersion;

        private final String conditionQuery;

        private final String expectedResult;

        private final List<String> queries = new LinkedList<>();

        public ConditionalQueryBuilder(VersionBuilder parent, String conditionQuery, String expectedResult) {
            parentVersion = parent;
            this.conditionQuery = conditionQuery;
            this.expectedResult = expectedResult;
        }

        public ConditionalQueryBuilder addQuery(String query) {
            queries.add(query);
            return this;
        }

        public ConditionalQueryBuilder addConditionalQuery(String conditionQuery, String expectedResult) {
            parentVersion.addConditionalUpgradeQuery(new ConditionalUpgradeQuery(
                    conditionQuery, expectedResult, queries
            ));
            return parentVersion.addConditionalQuery(conditionQuery, expectedResult);
        }

        public VersionListBuilder nextVersion() {
            parentVersion.addConditionalUpgradeQuery(new ConditionalUpgradeQuery(
                    conditionQuery, expectedResult, queries
            ));
            return parentVersion.next();
        }
    }
}