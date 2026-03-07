package com.github.roleplaycauldron.spellbook.database.updater.builder;

import java.util.*;

import com.github.roleplaycauldron.spellbook.database.updater.DatabaseVersion;

public class VersionListBuilder {

    private final List<DatabaseVersion> versions = new LinkedList<>();

    public VersionBuilder version(int versionNumber) {
        return new VersionBuilder(versionNumber, this);
    }

    public List<DatabaseVersion> finish() {
        return Collections.unmodifiableList(versions);
    }

    void version(DatabaseVersion version) {
        versions.add(version);
    }
}