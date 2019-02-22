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
    private Logger logger = LoggerFactory.getLogger(ConnectionPoolImpl.class);

    private AtomicBoolean activated = new AtomicBoolean(false);
    private AtomicInteger activatedCount = new AtomicInteger(0);

    private DatabaseNodeConfig databaseConfig;
    private DatabaseManager databaseManager;
    private LinkedList<Connection> freeConnections = new LinkedList<>();
    private LinkedList<Connection> activeConnections = new LinkedList<>();
    private ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    private ConnectionPoolImpl(DatabaseNodeConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.databaseManager = DatabaseManager.getInstance(this.databaseConfig);
    }

    public static ConnectionPool newInstance(DatabaseNodeConfig databaseConfig) {
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
    public synchronized Connection getConnection() {
        Connection connection;
        if (activatedCount.get() < this.databaseConfig.getPoolMaxSize()) {
            if (this.freeConnections.size() > 0) {
                connection = this.freeConnections.pollFirst();
                try {
                    if (this.databaseManager.isValidConnection(connection)) {
                        this.activeConnections.add(connection);
                        currentConnection.set(connection);
                    } else {
                        connection = this.getConnection();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
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
                this.wait(this.databaseConfig.getTimeout());
            } catch (InterruptedException e) {
                logger.warn("the waiting thread is interrupted!");
                e.printStackTrace();
            }
            if (this.databaseConfig.getTimeout() != 0 && System.currentTimeMillis() - startTime > this.databaseConfig.getTimeout()) {
                logger.warn("thread waiting for connection was time out!");
                return null;
            }
            connection = this.getConnection();
        }
        System.out.println("获取连接：======================活动连接个数====  " + this.activatedCount + "  ===========================游离连接个数======  " + this.freeConnections.size() + "  ================================================");
        return connection;
    }

    @Override
    public Connection newConnection() {
        return this.databaseManager.newConnection();
    }

    @Override
    public Connection getCurrentConnection() {
        Connection connection = currentConnection.get();
        try {
            if (!this.databaseManager.isValidConnection(connection)) {
                connection = this.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    public synchronized void releaseConn(Connection connection) throws SQLException {
        logger.info("{} release connection node: {}", Thread.currentThread().getName(), this.getDatabaseConfig().getNodeName());
        this.activeConnections.remove(connection);
        currentConnection.remove();
        if (this.databaseManager.isValidConnection(connection)) {
            this.freeConnections.add(connection);
        } else {
            this.freeConnections.add(this.newConnection());
        }
        this.notifyAll();
        System.out.println("释放链接：======================活动连接个数====  " + this.activatedCount + "  ===========================游离连接个数======  " + this.freeConnections.size() + "  ================================================");
    }

    @Override
    public synchronized void destroy() {
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
        scheduledExecutorService.scheduleAtFixedRate(() -> {

        }, 1, 5, TimeUnit.SECONDS);
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
        private ConnectionPool connectionPool;

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
