package org.homo.pocket.session;

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
        this.logger.info("transaction open.");
        this.connection.setAutoCommit(false);
    }

    @Override
    public void commit() throws SQLException {
        this.connection.commit();
        System.out.println("transaction commit.");
    }

    @Override
    public void rollBack() throws SQLException {
        System.out.println("transaction roll back.");
        this.connection.rollback();
    }
}
