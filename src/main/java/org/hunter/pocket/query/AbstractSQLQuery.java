package org.hunter.pocket.query;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/3
 */
abstract class AbstractSQLQuery {
    final Connection connection;
    final String sql;
    private Integer start;
    private Integer limit;
    Class clazz;
    final Map<String, Object> parameterMap = new HashMap<>();

    AbstractSQLQuery(String sql, Connection connection) {
        this.sql = sql;
        this.connection = connection;
    }

    AbstractSQLQuery(Connection connection, String sql, Class clazz) {
        this.connection = connection;
        this.sql = sql;
        this.clazz = clazz;
    }

    void setLimit(int start, int limit) {
        this.start = start;
        this.limit = limit;
    }

    boolean limited() {
        return this.start != null && this.limit != null;
    }

    Integer getStart() {
        return start;
    }

    Integer getLimit() {
        return limit;
    }
}
