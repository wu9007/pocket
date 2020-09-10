package org.hv.pocket.session;

import org.hv.pocket.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
public class TransactionImpl implements Transaction {
    private final Logger logger = LoggerFactory.getLogger(TransactionImpl.class);
    private final Connection connection;
    private volatile boolean isBegin;

    TransactionImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void begin() {
        try {
            if (!isBegin) {
                this.connection.setAutoCommit(false);
                isBegin = true;
                this.logger.debug("transaction 【begin】");
            } else {
                logger.warn("This transaction has already begun. Please do not try again");
            }
        } catch (SQLException e) {
            throw new TransactionException(e.getMessage());
        }
    }

    @Override
    public void commit() {
        if (isBegin) {
            try {
                this.connection.commit();
                this.connection.setAutoCommit(true);
                isBegin = false;
            } catch (SQLException e) {
                throw new TransactionException(e.getMessage());
            }
            this.logger.debug("Transaction 【commit】");
        } else {
            logger.warn("This transaction has been committed. Please do not try again");
        }
    }

    @Override
    public void rollBack() {
        if (isBegin) {
            try {
                this.connection.rollback();
                this.connection.setAutoCommit(true);
                isBegin = false;
            } catch (SQLException e) {
                throw new TransactionException(e.getMessage());
            }
            this.logger.debug("Transaction 【rollback】");
        } else {
            logger.warn("This transaction has been rolled back. Please do not try again");
        }
    }
}
