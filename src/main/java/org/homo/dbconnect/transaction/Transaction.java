package org.homo.dbconnect.transaction;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
public interface Transaction {

    /**
     * 开启事务
     *
     * @throws SQLException 开启失败
     */
    void transactionOn() throws SQLException;

    /**
     * 提交事务
     *
     * @throws SQLException 提交失败
     */
    void commit() throws SQLException;

    /**
     * 事务回滚
     *
     * @throws SQLException 回滚失败
     */
    void rollBack() throws SQLException;
}
