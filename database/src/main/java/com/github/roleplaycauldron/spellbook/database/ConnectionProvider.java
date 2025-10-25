package com.github.roleplaycauldron.spellbook.database;

import java.sql.*;

/**
 * General contract for a ConnectionProvider which might be implemented by a ConnectionPool
 * or central Database Managing Class
 */
public interface ConnectionProvider {

    /**
     * Returns a Database Connection
     *
     * @return a Database Connection
     * @throws SQLException thrown by the underlying JDBC Connector
     */
    Connection getConnection() throws SQLException;
}
