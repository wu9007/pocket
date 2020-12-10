package org.hv.pocket.connect;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author wujianchuan 2019/1/15
 */
public class ConnectionPoolImpl implements ConnectionPool {
    private final Logger logger = LoggerFactory.getLogger(ConnectionPoolImpl.class);

    private static final String CONNECT_LOCK = "CONNECT_MONITOR";
    private static final String RELEASE_LOCK = "RELEASE_MONITOR";
    private static final String DESTROY_LOCK = "DESTROY_MONITOR";

    private static final ThreadLocal<Integer> RETRY_TIMES = new ThreadLocal<>();

    private final AtomicBoolean activated = new AtomicBoolean(false);
    /**
     * 池中连接数
     */
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    /**
     * 数据库配置
     */
    private final DatabaseNodeConfig databaseConfig;
    private final DatabaseManager databaseManager;
    /**
     * 未被持有的连接
     */
    private final LinkedList<Connection> freeConnections = new LinkedList<>();
    /**
     * 已被持有的连接
     */
    private final LinkedList<Connection> activeConnections = new LinkedList<>();
    /**
     * 当前返回的的连接
     */
    private final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    private ConnectionPoolImpl(DatabaseNodeConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.databaseManager = DatabaseManager.getInstance(this.databaseConfig);
    }

    static ConnectionPool newInstance(DatabaseNodeConfig databaseConfig) {
        ConnectionPool instance = new ConnectionPoolImpl(databaseConfig);
        instance.init();
        return instance;
    }

    @Override
    public void init() {
        int initSize = this.databaseConfig.getPoolMiniSize();
        for (int index = 0; index < initSize; index++) {
            Connection connection = this.newConnection();
            this.freeConnections.add(connection);
            this.connectionCount.addAndGet(1);
            logger.debug("Add connection <{}> to <{}> success and Free connection count：{}.", connection, this.databaseConfig.getNodeName(), this.freeConnections.size());
        }
        if (this.activated.compareAndSet(false, true)) {
            this.checkPool();
        }
    }

    @Override
    public Connection getConnection() {
        synchronized (CONNECT_LOCK) {
            Connection connection;
            if (connectionCount.get() < this.databaseConfig.getPoolMaxSize()) {
                logger.debug("Free connection count：{}.", this.freeConnections.size());
                if (this.freeConnections.size() > 0) {
                    connection = this.freeConnections.pollFirst();
                    logger.debug("Free pool poll first connection node and current count : {}", freeConnections.size());
                    boolean connectIsValid = this.databaseManager.isValidConnection(connection);
                    logger.debug("Poll Connection-{} from free list\n This connect {} valid.", connection, connectIsValid ? "is" : "isn't");
                    if (connectIsValid) {
                        this.activeConnections.add(connection);
                        currentConnection.set(connection);
                    } else {
                        connection = this.newConnection();
                        logger.debug("Creat a new connection-{}.", connection);
                    }
                } else {
                    connection = this.newConnection();
                    logger.debug("Creat a new connection-{}.", connection);
                    this.activeConnections.add(connection);
                    currentConnection.set(connection);
                    this.connectionCount.incrementAndGet();
                }
            } else {
                try {
                    CONNECT_LOCK.wait(this.databaseConfig.getTimeout());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("the waiting thread is interrupted!");
                }
                if (RETRY_TIMES.get() == null) {
                    RETRY_TIMES.set(1);
                } else {
                    RETRY_TIMES.set(RETRY_TIMES.get() + 1);
                }
                logger.debug("The {} attempt to get a connection.", RETRY_TIMES.get());
                if (RETRY_TIMES.get() > this.databaseConfig.getRetry()) {
                    logger.warn("thread waiting for connection was time out!");
                    throw new ArrayIndexOutOfBoundsException("Sorry. The number of connections in the pool reaches the maximum number");
                }
                connection = this.getConnection();
            }
            RETRY_TIMES.remove();
            logger.debug("【Get】 Connect: Active-{} Free-{}", this.getActiveNum(), this.freeConnections.size());
            return connection;
        }
    }

    @Override
    public Connection newConnection() {
        return this.databaseManager.newConnection();
    }

    @Override
    public Connection getCurrentConnection() {
        Connection connection = currentConnection.get();
        if (!this.databaseManager.isValidConnection(connection)) {
            connection = this.getConnection();
        }
        return connection;
    }

    @Override
    public void releaseConn(Connection connection) {
        synchronized (RELEASE_LOCK) {
            logger.debug("Release Node: 【{}】", this.getDatabaseConfig().getNodeName());
            this.activeConnections.remove(connection);
            currentConnection.remove();
            if (this.databaseManager.isValidConnection(connection)) {
                this.freeConnections.add(connection);
                logger.debug("【Add to free】 Connect-{}: Active-{} Free-{}", connection, this.getActiveNum(), this.freeConnections.size());
            } else {
                this.freeConnections.add(this.newConnection());
                logger.debug("【New and add to free】 Connect-{}: Active-{} Free-{}", connection, this.getActiveNum(), this.freeConnections.size());
            }
            RELEASE_LOCK.notifyAll();
        }
    }

    @Override
    public void destroy() {
        synchronized (DESTROY_LOCK) {
            try {
                for (Connection freeConnection : this.freeConnections) {
                    freeConnection.close();
                }
                for (Connection activeConnection : this.activeConnections) {
                    activeConnection.close();
                }
            } catch (SQLException e) {
                logger.warn(e.getMessage());
            }
            this.activated.compareAndSet(true, false);
            this.freeConnections.clear();
            this.activeConnections.clear();
        }
    }

    @Override
    public boolean isActive() {
        return this.activated.get();
    }

    @Override
    public int getActiveNum() {
        return this.activeConnections.size();
    }

    @Override
    public int getFreeNum() {
        return this.freeConnections.size();
    }

    @Override
    public void pushToFreePool(Connection connection) {
        this.freeConnections.add(connection);
    }

    @Override
    public DatabaseNodeConfig getDatabaseConfig() {
        return this.databaseConfig;
    }

    /**
     * Regularly maintain the number and availability of links in the pool
     */
    private void checkPool() {
        String sql = "SELECT 0 FROM DUAL";
        String node = this.databaseConfig.getNodeName();
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2, new BasicThreadFactory.Builder().namingPattern(node + "-schedule-pool-%d").daemon(true).build());
        // The thread used to ensure link availability
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Collection<Connection> invalidConnections = this.freeConnections.stream().filter(connection -> {
                PreparedStatement preparedStatement = null;
                try {
                    logger.info("maintain connection - {}", connection);
                    preparedStatement = connection.prepareStatement(sql);
                    logger.debug("Creates a <code>PreparedStatement</code> object");
                    preparedStatement.executeQuery();
                    return false;
                } catch (SQLException e) {
                    logger.warn("this connection - {} is not available and needs to be removed\n exception message is - {}", connection, e.getMessage());
                    return true;
                } finally {
                    ConnectionManager.closeIo(preparedStatement, null);
                }
            }).collect(Collectors.toList());
            invalidConnections.forEach(invalidConnection -> {
                synchronized (RELEASE_LOCK) {
                    this.freeConnections.remove(invalidConnection);
                    this.connectionCount.getAndDecrement();
                }
            });
        }, databaseConfig.getAvailableInterval(), databaseConfig.getAvailableInterval(), TimeUnit.SECONDS);
        // Threads used to guarantee the number of links
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            DatabaseNodeConfig config = this.getDatabaseConfig();
            int totalConnection = this.getActiveNum() + this.getFreeNum();
            int lackConnection = config.getPoolMiniSize() - totalConnection;
            logger.info("{} - free connection count: {}", node, this.getFreeNum());
            logger.info("{} - activated connection count: {}", node, this.getActiveNum());
            if (lackConnection > 0) {
                logger.info("【{}】 - The database connection pool has 【{}】 connections that need to be supplemented ", config.getNodeName(), lackConnection);
                synchronized (RELEASE_LOCK) {
                    for (int index = 0; index < lackConnection; index++) {
                        this.freeConnections.add(newConnection());
                        this.connectionCount.getAndIncrement();
                    }
                }
            }
        }, databaseConfig.getMiniInterval(), databaseConfig.getMiniInterval(), TimeUnit.SECONDS);
    }
}
