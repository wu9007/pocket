package org.hunter.pocket.connect;

import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.exception.ErrorMessage;
import org.hunter.pocket.exception.PocketConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/31
 */
public class DatabaseManager {
    private static final Map<String, DatabaseManager> MANAGER_MAP = new HashMap<>(2);
    private final DatabaseNodeConfig config;

    private DatabaseManager(DatabaseNodeConfig config) {
        this.config = config;
        try {
            Class.forName(config.getDriverName());
        } catch (ClassNotFoundException e) {
            throw new PocketConnectionException(ErrorMessage.POCKET_DRIVER_CLASS_NOTFOUND_EXCEPTION);
        }
    }

    public static DatabaseManager getInstance(DatabaseNodeConfig config) {
        DatabaseManager databaseManager = MANAGER_MAP.get(config.getNodeName());
        if (databaseManager == null) {
            MANAGER_MAP.put(config.getNodeName(), new DatabaseManager(config));
        }
        return MANAGER_MAP.get(config.getNodeName());
    }

    Connection newConnection() {
        Connection conn;
        try {
            conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
        } catch (SQLException e) {
            throw new PocketConnectionException(ErrorMessage.POCKET_CONNECT_DATABASE_EXCEPTION);
        }
        return conn;
    }

    Boolean isValidConnection(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(Math.toIntExact(config.getTimeout()));
        } catch (SQLException e) {
            throw new PocketConnectionException(ErrorMessage.POCKET_CONNECTION_VARIABLE_EXCEPTION);
        }
    }
}
