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
    public synchronized void begin() throws SQLException {
        if (this.connection.getAutoCommit()) {
            this.connection.setAutoCommit(false);
            this.logger.info("transaction open.");
        } else {
            logger.warn("This transaction has already begun. Please do not try again.");
        }
    }

    @Override
    public synchronized void commit() throws SQLException {
        if (this.connection != null) {
            this.connection.commit();
            this.connection = null;
            this.logger.info("Transaction commit.");
        } else {
            logger.warn("This transaction has been committed. Please do not try again.");
        }
    }

    @Override
    public synchronized void rollBack() throws SQLException {
        if (this.connection != null) {
            this.connection.rollback();
            this.connection = null;
            this.logger.info("Transaction rollback.");
        } else {
            logger.warn("This transaction has been rolled back. Please do not try again.");
        }
    }
}
