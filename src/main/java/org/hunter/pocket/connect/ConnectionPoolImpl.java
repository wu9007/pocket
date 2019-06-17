package org.hunter.pocket.connect;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wujianchuan 2019/1/15
 */
public class ConnectionPoolImpl implements ConnectionPool {
    private final Logger logger = LoggerFactory.getLogger(ConnectionPoolImpl.class);
    private static final String CONNECT_LOCK = "CONNECT_MONITOR";
    private static final String RELEASE_LOCK = "RELEASE_MONITOR";
    private static final String DESTROY_LOCK = "DESTROY_MONITOR";

    private static ThreadLocal<Integer> retryTimes = new ThreadLocal<>();

    private final AtomicBoolean activated = new AtomicBoolean(false);
    private final AtomicInteger activatedCount = new AtomicInteger(0);

    private final DatabaseNodeConfig databaseConfig;
    private final DatabaseManager databaseManager;
    private final LinkedList<Connection> freeConnections = new LinkedList<>();
    private final LinkedList<Connection> activeConnections = new LinkedList<>();
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
            this.activatedCount.addAndGet(1);
        }
        this.activated.compareAndSet(false, true);
    }

    @Override
    public Connection getConnection() {
        synchronized (CONNECT_LOCK) {
            Connection connection;
            if (activatedCount.get() < this.databaseConfig.getPoolMaxSize()) {
                if (this.freeConnections.size() > 0) {
                    connection = this.freeConnections.pollFirst();
                    if (this.databaseManager.isValidConnection(connection)) {
                        this.activeConnections.add(connection);
                        currentConnection.set(connection);
                    } else {
                        connection = this.getConnection();
                    }
                } else {
                    connection = this.newConnection();
                    this.activeConnections.add(connection);
                    currentConnection.set(connection);
                    this.activatedCount.incrementAndGet();
                }
            } else {
                long startTime = System.currentTimeMillis();
                try {
                    CONNECT_LOCK.wait(this.databaseConfig.getTimeout());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("the waiting thread is interrupted!");
                    e.printStackTrace();
                }
                if (retryTimes.get() == null) {
                    retryTimes.set(1);
                } else {
                    retryTimes.set(retryTimes.get() + 1);
                }
                logger.info("====================== The {} attempt to get a connection. ======================", retryTimes.get());
                if (retryTimes.get() > this.databaseConfig.getRetry()) {
                    logger.warn("thread waiting for connection was time out!");
                    return null;
                }
                connection = this.getConnection();
            }
            retryTimes.remove();
            logger.info("Get the connection:====================== Number of active connections: {}  =========================== Number of free connections: {}================================",
                    this.activatedCount, this.freeConnections.size());
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
            logger.info("{} release connection node: {}", Thread.currentThread().getName(), this.getDatabaseConfig().getNodeName());
            this.activeConnections.remove(connection);
            currentConnection.remove();
            if (this.databaseManager.isValidConnection(connection)) {
                this.freeConnections.add(connection);
            } else {
                this.freeConnections.add(this.newConnection());
            }
            RELEASE_LOCK.notifyAll();
            logger.debug("Release connection:====================== Number of active connections: {} =========================== Number of free connections: {} " +
                            "==============================",
                    this.activatedCount, this.freeConnections.size());

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
                e.printStackTrace();
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
    public void checkPool() {
        String node = this.databaseConfig.getNodeName();
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2, new BasicThreadFactory.Builder().namingPattern(node + "-schedule-pool-%d").daemon(true).build());

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            logger.info("{} - free connection count: {}", node, this.getFreeNum());
            logger.info("{} - activated connection count: {}", node, this.getActiveNum());
        }, 1, 1, TimeUnit.SECONDS);
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

    class CheckFreePool extends TimerTask {
        private final ConnectionPool connectionPool;

        public CheckFreePool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
        }

        @Override
        public void run() {
            if (this.connectionPool != null && this.connectionPool.isActive()) {
                DatabaseNodeConfig config = this.connectionPool.getDatabaseConfig();
                int totalConnection = this.connectionPool.getActiveNum() + this.connectionPool.getFreeNum();
                int lackConnection = config.getPoolMiniSize() - totalConnection;
                if (lackConnection > 0) {
                    logger.info("{} - The database connection pool has {} connections that need to be supplemented ", config.getNodeName(), lackConnection);
                    for (int index = 0; index < lackConnection; index++) {
                        this.connectionPool.pushToFreePool(connectionPool.newConnection());
                    }
                }
            }
        }
    }
}
