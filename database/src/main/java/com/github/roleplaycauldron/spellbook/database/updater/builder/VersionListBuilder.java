package com.github.roleplaycauldron.spellbook.database.updater.builder;

import java.util.*;

import com.github.roleplaycauldron.spellbook.database.updater.DatabaseVersion;

/**
 * A builder for a list of database versions
 */
public class VersionListBuilder {

    /**
     * List of versions this builder is building
     */
    private final List<DatabaseVersion> versions = new LinkedList<>();

    /**
     * Creates a new version by providing its version number. These have to be bigger than zero and unique
     *
     * @param versionNumber the version number, bigger than zero and unique
     * @return a new VersionBuilder
     */
    public VersionBuilder version(int versionNumber) {
        if (versionNumber <= 0) {
            throw new IllegalArgumentException("Version number must be positive");
        }
        if (versions.stream().anyMatch(v -> v.versionNumber() == versionNumber)) {
            throw new IllegalArgumentException("Version number must be unique");
        }
        return new VersionBuilder(versionNumber, this);
    }

    /**
     * Finish building the list
     *
     * @return the list of versions
     */
    public List<DatabaseVersion> finish() {
        return Collections.unmodifiableList(versions);
    }

    /**
     * Adds a version to the list builder.
     * Package-private access so the {@link VersionBuilder} can use it without it being part of the public API
     *
     * @param version the version to add
     */
    void version(DatabaseVersion version) {
        versions.add(version);
    }
}