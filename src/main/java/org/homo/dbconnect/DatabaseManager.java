package org.homo.dbconnect;

import org.homo.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author wujianchuan 2018/12/31
 */
@Component
public class DatabaseManager {
    private final
    DatabaseConfig config;
    private static Connection conn = null;

    @Autowired
    private DatabaseManager(DatabaseConfig config) {
        this.config = config;
        try {
            Class.forName(config.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
