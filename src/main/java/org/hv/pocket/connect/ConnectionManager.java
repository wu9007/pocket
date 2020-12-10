package org.hv.pocket.connect;

import org.hv.pocket.config.DatabaseConfig;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.exception.ErrorMessage;
import org.hv.pocket.exception.PocketConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/1/15
 */
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private static final ConnectionManager OUR_INSTANCE = new ConnectionManager();
    private final Map<String/* node name */, ConnectionPool/* connection pool */> connectionPoolMap = new ConcurrentHashMap<>(4);

    public synchronized void register(DatabaseConfig databaseConfig) {
        databaseConfig.getNode().forEach(databaseNode -> {
            boolean success = verify(databaseNode);
            ConnectionPool connectionPool = ConnectionPoolImpl.newInstance(databaseNode);
            if (success) {
                this.connectionPoolMap.put(databaseNode.getNodeName(), connectionPool);
            } else {
                throw new PocketConnectionException(ErrorMessage.POCKET_NODE_NOTFOUND_EXCEPTION);
            }
        });
    }

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return OUR_INSTANCE;
    }

    public Connection getConnection(DatabaseNodeConfig databaseNodeConfig) {
        return this.connectionPoolMap.get(databaseNodeConfig.getNodeName()).getConnection();
    }

    public void closeConnection(String node, Connection connection) {
        ConnectionPool connectionPool = this.connectionPoolMap.get(node);
        if (connectionPool != null) {
            connectionPool.releaseConn(connection);
        } else {
            logger.error("The database link failed to recover because it could not find the pool named: {}", node);
        }
    }

    public void destroy() {
        this.connectionPoolMap.forEach((key, value) -> value.destroy());
        logger.debug("Database pool was destroy");
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
                                if (config.getCollectLog() == null) {
                                    config.setCollectLog(false);
                                }
                                if (config.getPoolMiniSize() == null) {
                                    config.setPoolMiniSize(20);
                                }
                                if (config.getPoolMaxSize() == null) {
                                    config.setPoolMaxSize(100);
                                }
                                if (config.getTimeout() == null) {
                                    config.setTimeout(5L);
                                }
                                if (config.getRetry() == null) {
                                    config.setRetry(5);
                                }
                                if (config.getMiniInterval() == null) {
                                    config.setMiniInterval(36000);
                                }
                                if (config.getAvailableInterval() == null) {
                                    config.setAvailableInterval(1800);
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

    public static void closeIo(PreparedStatement preparedStatement, ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            if (preparedStatement != null && !preparedStatement.isClosed()) {
                preparedStatement.close();
                logger.debug("Releases a <code>PreparedStatement</code> object");
            }
        } catch (Exception e) {
            throw new PocketConnectionException(ErrorMessage.POCKET_IO_RELEASE_EXCEPTION);
        }
    }
}
