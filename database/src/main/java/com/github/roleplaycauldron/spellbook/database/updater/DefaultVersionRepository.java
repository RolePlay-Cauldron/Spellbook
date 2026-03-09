package com.github.roleplaycauldron.spellbook.database.updater;

import java.util.List;

/**
 * <p>Default implementation of {@link VersionRepositoryBase}.</p>
 * <p>Only requires the database versions and will follow the
 * logic described in the {@link DefaultVersionRepository} Javadoc.</p>
 * <p>Use {@link VersionRepositoryBase#builder()} to easily create
 * a List of DatabaseVersions using the Builder pattern</p>
 */
public class DefaultVersionRepository extends VersionRepositoryBase {

    /**
     * Constructor taking a list of database versions, see {@link VersionRepositoryBase#builder()}
     *
     * @param versions the database versions
     */
    public DefaultVersionRepository(List<DatabaseVersion> versions) {
        super(versions);
    }
}
