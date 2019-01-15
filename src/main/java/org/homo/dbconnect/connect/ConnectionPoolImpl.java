package org.homo.dbconnect.connect;

import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wujianchuan 2019/1/15
 */
public class ConnectionPoolImpl implements ConnectionPool {
    private Logger logger = LoggerFactory.getLogger(ConnectionPoolImpl.class);

    private AtomicBoolean activated = new AtomicBoolean(false);
    private AtomicInteger activatedCount = new AtomicInteger(0);

    private AbstractDatabaseConfig databaseConfig;
    private DatabaseManager databaseManager;
    private LinkedList<Connection> freeConnections = new LinkedList<>();
    private LinkedList<Connection> activeConnections = new LinkedList<>();
    private ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    private ConnectionPoolImpl(AbstractDatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.databaseManager = DatabaseManager.getInstance(this.databaseConfig);
    }

    public static ConnectionPool newInstance(AbstractDatabaseConfig databaseConfig) {
        ConnectionPool instance = new ConnectionPoolImpl(databaseConfig);
        instance.init();
        return instance;
    }

    @Override
    public void init() {
        int initSize = this.databaseConfig.getPoolMaxSize();
        for (int index = 0; index < initSize; index++) {
            Connection connection = this.newConnection();
            this.freeConnections.add(connection);
            this.activatedCount.addAndGet(1);
        }
        this.activated.compareAndSet(true, false);
    }

    @Override
    public synchronized Connection getConnection() {
        Connection connection;
        if (activatedCount.incrementAndGet() < this.databaseConfig.getPoolMaxSize()) {
            if (this.freeConnections.size() > 0) {
                connection = this.freeConnections.pollFirst();
                try {
                    if (this.databaseManager.isValidConnection(connection)) {
                        this.activeConnections.add(connection);
                        this.currentConnection.set(connection);
                    } else {
                        connection = this.getConnection();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                connection = this.newConnection();
                this.activeConnections.add(connection);
                this.currentConnection.set(connection);
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
        return connection;
    }

    @Override
    public Connection newConnection() {
        return this.databaseManager.newConnection();
    }

    @Override
    public Connection getCurrentConnection() {
        Connection connection = this.currentConnection.get();
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
        logger.info("{} release connection: {}", Thread.currentThread().getName(), connection);
        this.activeConnections.remove(connection);
        this.currentConnection.remove();
        if (this.databaseManager.isValidConnection(connection)) {
            this.freeConnections.add(connection);
        } else {
            this.freeConnections.add(this.newConnection());
        }
        this.notifyAll();
    }

    @Override
    public synchronized void destroy() {
        currentConnection.remove();
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void checkPool() {

    }

    @Override
    public int getActiveNum() {
        return 0;
    }

    @Override
    public int getFreeNum() {
        return 0;
    }
}
