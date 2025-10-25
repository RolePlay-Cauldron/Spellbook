package com.github.roleplaycauldron.spellbook.database;

import java.sql.Connection;

/**
 * General contract for a ConnectionProvider which might be implemented by a ConnectionPool
 * or central Database Managing Class
 */
public interface ConnectionProvider {

    /**
     * Returns a Database Connection
     *
     * @return a Database Connection
     */
    Connection getConnection();
}
