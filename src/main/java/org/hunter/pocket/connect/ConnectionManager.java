package org.hunter.pocket.connect;

import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/1/15
 */
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private static final ConnectionManager ourInstance = new ConnectionManager();
    private final Map<String, ConnectionPool> connectionPoolMap = new ConcurrentHashMap<>(4);

    public synchronized void register(DatabaseConfig databaseConfig) {
        databaseConfig.getNode().forEach(databaseNode -> {
            ConnectionPool connectionPool = ConnectionPoolImpl.newInstance(databaseNode);
            boolean success = verify(databaseNode);
            if (success) {
                this.connectionPoolMap.put(databaseNode.getNodeName(), connectionPool);
            } else {
                throw new NullPointerException("Configuration error");
            }
        });
    }

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return ourInstance;
    }

    public Connection getConnection(DatabaseNodeConfig databaseNodeConfig) {
        return this.connectionPoolMap.get(databaseNodeConfig.getNodeName()).getConnection();
    }

    public void closeConnection(String node, Connection connection) {
        ConnectionPool connectionPool = this.connectionPoolMap.get(node);
        if (connectionPool != null) {
            try {
                connectionPool.releaseConn(connection);
            } catch (SQLException e) {
                logger.error("The database link failed to recover");
                e.printStackTrace();
            }
        } else {
            logger.error("The database link failed to recover because it could not find the pool named: {}", node);
        }
    }

    public void destroy() {
        this.connectionPoolMap.forEach((key, value) -> value.destroy());
        logger.info("Database pool was destroy");
    }

    private static boolean verify(DatabaseNodeConfig config) {
        if (!config.getNodeName().isEmpty()) {
            if (!config.getUrl().isEmpty()) {
                if (!config.getDriverName().isEmpty()) {
                    if (!config.getUser().isEmpty()) {
                        if (!config.getPassword().isEmpty()) {
                            if (!config.getSession().isEmpty()) {
                                if (config.getShowSql() == null) {
                                    config.setShowSql(false);
                                }
                                if (config.getPoolMiniSize() == null) {
                                    config.setPoolMiniSize(20);
                                }
                                if (config.getPoolMaxSize() == null) {
                                    config.setPoolMaxSize(100);
                                }
                                if (config.getTimeout() == null) {
                                    config.setTimeout(2000L);
                                }
                            } else {
                                logger.error("Please configure the session correctly");
                                return false;
                            }
                        } else {
                            logger.error("Please configure the password correctly");
                            return false;
                        }
                    } else {
                        logger.error("Please configure the user correctly");
                        return false;
                    }
                } else {
                    logger.error("Please configure the driver correctly");
                    return false;
                }
            } else {
                logger.error("Please configure the url correctly");
                return false;
            }
        } else {
            logger.error("Please configure the node correctly");
            return false;
        }
        return true;
    }

    public static void closeIO(PreparedStatement preparedStatement, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
