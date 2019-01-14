package org.homo.dbconnect;

import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.transaction.TransactionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/31
 */
public class DatabaseManager {
    private static Map<String, DatabaseManager> managerMap = new HashMap<>(2);
    private Logger logger = LoggerFactory.getLogger(TransactionImpl.class);
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
        DatabaseManager databaseManager = managerMap.get(config.getDatabaseName());
        if (databaseManager == null) {
            managerMap.put(config.getDatabaseName(), new DatabaseManager(config));
        }
        return managerMap.get(config.getDatabaseName());
    }

    public Connection getConn() {
        try {
            conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
            this.logger.info("CONNECTION: open - {}", this.config.getDatabaseName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void closeConn(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                this.logger.info("CONNECTION: close - {}", this.config.getDatabaseName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
