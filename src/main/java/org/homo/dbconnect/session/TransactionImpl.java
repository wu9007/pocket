package org.homo.dbconnect.session;

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

    TransactionImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void begin() throws SQLException {
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
