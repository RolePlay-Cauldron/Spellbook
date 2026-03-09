package com.github.roleplaycauldron.spellbook.database.updater.builder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.github.roleplaycauldron.spellbook.database.updater.ConditionalUpgradeQuery;
import com.github.roleplaycauldron.spellbook.database.updater.DatabaseVersion;

/**
 * <p>Builder for a single Database Version</p>
 * <p>Queries will be prioritized in the same order they are added by this Builder.</p>
 */
public class VersionBuilder {

    /**
     * Builder for a List of Database Versions that created this builder
     */
    private final VersionListBuilder parentList;

    /**
     * Version number for this builder
     */
    private final int versionNumber;

    /**
     * List of conditional upgrade queries
     */
    private final List<ConditionalUpgradeQuery> conditionalUpgradeQueries = new LinkedList<>();

    /**
     * List of special queries run on the first startup if this is the most recent version containing said queries
     */
    private final List<String> firstStartupQueries = new LinkedList<>();

    /**
     * Map of Priority to Queries that are run unconditionally
     */
    private final Map<Integer, String> unconditionalQueries = new LinkedHashMap<>();

    /**
     * Tracker for the highest priority already assigned to a query
     */
    private final AtomicInteger highestPriority = new AtomicInteger(0);

    /**
     * Creates a new VersionBuilder with the given version number and parent list builder
     *
     * @param versionNumber the version number for this builder
     * @param parent        the parent list builder that created this builder
     */
    public VersionBuilder(int versionNumber, VersionListBuilder parent) {
        this.versionNumber = versionNumber;
        this.parentList = parent;
    }

    /**
     * Add a query to be run on the first startup if this is the most recent version containing said queries.
     *
     * @param query the query to add
     * @return this builder for chaining
     */
    public VersionBuilder addFirstStartupQuery(String query) {
        firstStartupQueries.add(query);
        return this;
    }

    /**
     * <p>Add a set of conditional queries. First the <code>conditionQuery</code> will be run
     * and the first result will be compared to <code>expectedResult</code>.</p>
     * <p>Only if the results match the queries provided next will be run</p>
     * <p>If conditionQuery or expectedResult are null/empty, the queries are handled as unconditional</p>
     * <p>Queries will be prioritized in the same order they are added by this Builder.</p>
     *
     * @param conditionQuery the query to run to check if the upgrade should be applied
     * @param expectedResult the expected result of the condition query
     * @return a builder for the conditional query
     */
    public ConditionalQueryBuilder addConditionalQuery(String conditionQuery, String expectedResult) {
        return new ConditionalQueryBuilder(this, conditionQuery, expectedResult);
    }

    /**
     * <p>Adds an unconditional query to this version. This query will be run without a condition check.</p>
     * <p>Queries will be prioritized in the same order they are added by this Builder.</p>
     *
     * @param query the query to run
     * @return this builder
     */
    public VersionBuilder addUnconditionalQuery(String query) {
        unconditionalQueries.put(highestPriority.getAndIncrement(), query);
        return this;
    }

    /**
     * Finish constructing this version and continue with the next version or finish the list.
     *
     * @return the parent list builder
     */
    public VersionListBuilder finishVersion() {
        List<ConditionalUpgradeQuery> combinedQueries = unconditionalQueries.entrySet()
                .stream()
                .map(e -> new ConditionalUpgradeQuery(
                        e.getKey(),
                        null,
                        null,
                        List.of(e.getValue())
                ))
                .collect(Collectors.toList());

        combinedQueries.addAll(conditionalUpgradeQueries);
        parentList.version(new DatabaseVersion(versionNumber, combinedQueries, firstStartupQueries));
        return parentList;
    }

    /**
     * Adds a upgrade query to the list of queries for this version.
     * It does not require a condition to be fulfilled to be run.
     *
     * @param query the query to add
     */
    private void addConditionalUpgradeQuery(ConditionalUpgradeQuery query) {
        conditionalUpgradeQueries.add(query);
    }

    /**
     * Builder for conditional queries
     */
    public static class ConditionalQueryBuilder {

        /**
         * The parent version builder
         */
        private final VersionBuilder parentVersion;

        /**
         * The condition query
         */
        private final String conditionQuery;

        /**
         * Expected result of the condition query
         */
        private final String expectedResult;

        /**
         * Queries to run if the condition query is ok
         */
        private final List<String> queries = new LinkedList<>();

        private ConditionalQueryBuilder(VersionBuilder parent, String conditionQuery, String expectedResult) {
            parentVersion = parent;
            this.conditionQuery = conditionQuery;
            this.expectedResult = expectedResult;
        }

        /**
         * Adds a query to the list of queries to run if the condition query is ok
         *
         * @param query the query to add
         * @return this builder
         */
        public ConditionalQueryBuilder addQuery(String query) {
            queries.add(query);
            return this;
        }

        /**
         * Finish constructing this conditional query and continue with another.
         * See {@link VersionBuilder#addConditionalQuery(String, String)} for more information.
         *
         * @param conditionQuery the next condition query
         * @param expectedResult the expected result of the next condition query
         * @return a builder for the next conditional query
         */
        public ConditionalQueryBuilder addConditionalQuery(String conditionQuery, String expectedResult) {
            parentVersion.addConditionalUpgradeQuery(new ConditionalUpgradeQuery(
                    parentVersion.highestPriority.getAndIncrement(), conditionQuery, expectedResult, queries
            ));
            return parentVersion.addConditionalQuery(conditionQuery, expectedResult);
        }

        /**
         * Add an unconditional query to the currently worked on version
         * See {@link VersionBuilder#addUnconditionalQuery(String)} for more information.
         *
         * @param query the query to add
         * @return the builder for the current version
         */
        public VersionBuilder addUnconditionalQuery(String query) {
            addSelfToParent();
            return parentVersion.addUnconditionalQuery(query);
        }

        /**
         * Return to the current version
         *
         * @return the builder for the current version
         */
        public VersionBuilder backToCurrentVersion() {
            addSelfToParent();
            return parentVersion;
        }

        /**
         * Continue with the next version
         *
         * @return the builder for the next version
         */
        public VersionListBuilder nextVersion() {
            addSelfToParent();
            return parentVersion.finishVersion();
        }

        private void addSelfToParent() {
            parentVersion.addConditionalUpgradeQuery(new ConditionalUpgradeQuery(
                    parentVersion.highestPriority.getAndIncrement(), conditionQuery, expectedResult, queries
            ));
        }
    }
}