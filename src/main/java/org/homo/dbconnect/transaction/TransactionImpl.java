package org.homo.dbconnect.transaction;

import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
public class TransactionImpl implements Transaction {
    private Logger logger = LoggerFactory.getLogger(TransactionImpl.class);
    private Connection connection;
    private DatabaseManager databaseManager;

    public TransactionImpl(AbstractDatabaseConfig databaseConfig) {
        this.databaseManager = DatabaseManager.getInstance(databaseConfig);
    }

    @Override
    public void connect() {
        this.connection = this.databaseManager.getConn();
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void closeConnection() {
        this.databaseManager.closeConn(this.connection);
    }

    @Override
    public void transactionOn() throws SQLException {
        this.logger.info("TRANSACTION: open.");
        this.connection.setAutoCommit(false);
    }

    @Override
    public void commit() throws SQLException {
        System.out.println("TRANSACTION: commit.");
        this.connection.commit();
    }

    @Override
    public void rollBack() throws SQLException {
        System.out.println("TRANSACTION: roll back.");
        this.connection.rollback();
    }
}
