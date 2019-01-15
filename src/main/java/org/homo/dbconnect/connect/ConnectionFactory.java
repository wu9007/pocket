package org.homo.dbconnect.connect;

import org.homo.dbconnect.config.AbstractDatabaseConfig;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/1/15
 */
public class ConnectionFactory {
    private static ConnectionFactory ourInstance = new ConnectionFactory();
    private static Map<String, LinkedList<Connection>> databasePool = new ConcurrentHashMap<>(4);

    public static void register(AbstractDatabaseConfig databaseConfig) {
        LinkedList<Connection> connections = new LinkedList<>();
        databasePool.put(databaseConfig.getDatabaseName(), connections);
    }

    public static ConnectionFactory getInstance() {
        return ourInstance;
    }

    private ConnectionFactory() {
    }

    public Connection getConnection(AbstractDatabaseConfig databaseConfig) {
        //TODO:连接池
        return DatabaseManager.getInstance(databaseConfig).newConnection();
    }
}
