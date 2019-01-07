package org.homo.dbconnect.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
public interface Transaction {

    /**
     * 连接数据库
     */
    void connect();

    /**
     * 获取当前数据库连接
     *
     * @return 数据库连接对象
     */
    Connection getConnection();

    /**
     * 关闭数据库连接
     *
     * @param preparedStatement PreparedStatement
     * @param rs                ResultSet
     */
    void closeConnection(PreparedStatement preparedStatement, ResultSet rs);

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
