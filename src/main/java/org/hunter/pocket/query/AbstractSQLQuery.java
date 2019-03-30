package org.hunter.pocket.query;

import java.sql.Connection;

/**
 * @author wujianchuan 2019/1/3
 */
abstract class AbstractSQLQuery {
    final Connection connection;
    final String sql;
    //TODO 分页
    private Integer start;
    private Integer limit;
    Class clazz;

    AbstractSQLQuery(String sql, Connection connection) {
        this.sql = sql;
        this.connection = connection;
    }

    public AbstractSQLQuery(Connection connection, String sql, Class clazz) {
        this.connection = connection;
        this.sql = sql;
        this.clazz = clazz;
    }
}
