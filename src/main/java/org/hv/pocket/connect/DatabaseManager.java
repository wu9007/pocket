package org.hv.pocket.connect;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.exception.ErrorMessage;
import org.hv.pocket.exception.PocketConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/31
 */
public class DatabaseManager {
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
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
            logger.info("Attempts to establish a connection to the given database URL.");
            DriverManager.setLoginTimeout(Math.toIntExact(config.getTimeout()));
            conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
        } catch (SQLException e) {
            logger.error("\nurl:{}\nuser:{}\npassword:{}", config.getUrl(), config.getUser(), config.getPassword());
            throw new PocketConnectionException(String.format(ErrorMessage.POCKET_CONNECT_DATABASE_EXCEPTION, e.getMessage()));
        }
        return conn;
    }

    Boolean isValidConnection(Connection connection) {
        boolean connectionIsValid = false;
        try {
            logger.debug("Connection {} null, {}", (connection == null ? "is" : "isn't"),
                    (connection != null && connection.isClosed() ? "Connection is closed" : ""));
            if (connection != null && !connection.isClosed()) {
                connectionIsValid = connection.isValid(Math.toIntExact(config.getTimeout()));
            }
            return connectionIsValid;
        } catch (SQLException e) {
            throw new PocketConnectionException(ErrorMessage.POCKET_CONNECTION_VARIABLE_EXCEPTION);
        }
    }
}
