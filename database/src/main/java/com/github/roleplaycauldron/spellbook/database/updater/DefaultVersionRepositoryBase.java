package com.github.roleplaycauldron.spellbook.database.updater;

import java.util.List;

public class DefaultVersionRepositoryBase extends VersionRepositoryBase {

    public DefaultVersionRepositoryBase(List<DatabaseVersion> versions) {
        super(versions);
    }
}
