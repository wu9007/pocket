package org.homo.dbconnect.connect;

import org.homo.dbconnect.config.AbstractDatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/31
 */
public class DatabaseManager {
    private static Map<String, DatabaseManager> managerMap = new HashMap<>(2);
    private AbstractDatabaseConfig config;
    private static Connection conn = null;

    private DatabaseManager(AbstractDatabaseConfig config) {
        this.config = config;
        try {
            Class.forName(config.getDriverName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance(AbstractDatabaseConfig config) {
        DatabaseManager databaseManager = managerMap.get(config.getNode());
        if (databaseManager == null) {
            managerMap.put(config.getNode(), new DatabaseManager(config));
        }
        return managerMap.get(config.getNode());
    }

    Connection newConnection() {
        try {
            conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    Boolean isValidConnection(Connection connection) throws SQLException {
        return connection != null && !connection.isClosed();
    }
}
