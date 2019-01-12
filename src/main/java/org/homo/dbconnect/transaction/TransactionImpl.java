package org.homo.dbconnect.transaction;

import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
public class TransactionImpl implements Transaction {
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
        System.out.println("开启事务");
        this.connection.setAutoCommit(false);
    }

    @Override
    public void commit() throws SQLException {
        System.out.println("提交事务");
        this.connection.commit();
    }

    @Override
    public void rollBack() throws SQLException {
        System.out.println("事务回滚");
        this.connection.rollback();
    }
}
