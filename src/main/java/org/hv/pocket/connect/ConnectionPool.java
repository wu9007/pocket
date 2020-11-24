package org.hv.pocket.connect;

import org.hv.pocket.config.DatabaseNodeConfig;

import java.sql.Connection;

/**
 * @author wujianchuan 2019/1/15
 */
interface ConnectionPool {

    /**
     * 初始化连接池
     */
    void init();

    /**
     * 从池中获取链接
     *
     * @return 数据库链接对象
     */
    Connection getConnection();

    /**
     * 新建数据库链接对象
     *
     * @return 数据库链接对象
     */
    Connection newConnection();

    /**
     * 获取当前数据库链接对象
     *
     * @return 数据库链接对象
     */
    Connection getCurrentConnection();

    /**
     * 释放链接
     *
     * @param connection 数据库链接对象
     */
    void releaseConn(Connection connection);

    /**
     * 销毁多有链接
     */
    void destroy();

    /**
     * 是否处于激活状态
     *
     * @return 是否
     */
    boolean isActive();

    /**
     * 获取被激活的链接数量
     *
     * @return 被激活的链接数量
     */
    int getActiveNum();

    /**
     * 获取闲置链接的数量
     *
     * @return 闲置链接的数量
     */
    int getFreeNum();

    /**
     * 向闲置链接池中放入链接对象
     *
     * @param connection 连接对象
     */
    void pushToFreePool(Connection connection);

    /**
     * 获取数据库配置信息
     *
     * @return 数据库配置对象
     */
    DatabaseNodeConfig getDatabaseConfig();
}
