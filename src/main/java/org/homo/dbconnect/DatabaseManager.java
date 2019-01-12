package org.homo.dbconnect;

import org.homo.config.AbstractDatabaseConfig;
import org.homo.config.MysqlConfig;

import java.sql.Connection;
import java.sql.DriverManager;
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

    public static DatabaseManager getInstance(String databaseName) {
        return managerMap.get(databaseName);
    }

    public void registerConfig(AbstractDatabaseConfig config) {
        managerMap.put(config.getDatabaseName(), new DatabaseManager(config));
    }

    public Connection getConn() {
        try {
            conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
            System.out.println("开启数据库连接");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void closeConn(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("关闭数据库连接");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
