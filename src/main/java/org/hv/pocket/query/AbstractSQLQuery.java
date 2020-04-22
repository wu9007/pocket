package org.hv.pocket.query;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.logger.StatementProxy;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/3
 */
abstract class AbstractSQLQuery {
    final StatementProxy statementProxy;
    final Connection connection;
    private final DatabaseNodeConfig databaseNodeConfig;
    final String sql;
    private Integer start;
    private Integer limit;
    Class<?> clazz;
    final Map<String, Object> parameterMap = new HashMap<>();
    final List<String> columnNameList = new LinkedList<>();

    AbstractSQLQuery(String sql, Connection connection, DatabaseNodeConfig databaseNodeConfig) {
        this.sql = sql;
        this.connection = connection;
        this.databaseNodeConfig = databaseNodeConfig;
        this.statementProxy = StatementProxy.newInstance(this.databaseNodeConfig);
    }

    AbstractSQLQuery(Connection connection, String sql, DatabaseNodeConfig databaseNodeConfig, Class<?> clazz) {
        this.sql = sql;
        this.connection = connection;
        this.databaseNodeConfig = databaseNodeConfig;
        this.statementProxy = StatementProxy.newInstance(this.databaseNodeConfig);
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
