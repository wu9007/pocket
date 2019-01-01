package org.homo.dbconnect;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
class HomoTransaction implements Transaction {
    private Connection connection;

    HomoTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void transactionOn() throws SQLException {
        this.connection.setAutoCommit(false);
    }

    @Override
    public void commit() throws SQLException {
        this.connection.commit();
    }

    @Override
    public void rollBack() throws SQLException {
        this.connection.rollback();
    }
}
