package org.hv.pocket.query;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.criteria.ParameterTranslator;
import org.hv.pocket.criteria.PersistenceProxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/3
 */
abstract class AbstractSqlQuery {
    boolean batchExecution = false;
    final PersistenceProxy persistenceProxy;
    final Connection connection;
    final DatabaseNodeConfig databaseNodeConfig;
    String sql;
    PreparedStatement preparedStatement;
    private Integer start;
    private Integer limit;
    Class<?> clazz;
    final Map<String, Object> parameterMap = new HashMap<>();
    final List<ParameterTranslator> queryParameters = new LinkedList<>();
    final List<String> columnNameList = new LinkedList<>();

    AbstractSqlQuery(Connection connection, DatabaseNodeConfig databaseNodeConfig) {
        this.connection = connection;
        this.databaseNodeConfig = databaseNodeConfig;
        this.persistenceProxy = PersistenceProxy.newInstance(this.databaseNodeConfig);
    }

    AbstractSqlQuery(String sql, Connection connection, DatabaseNodeConfig databaseNodeConfig) {
        // NOTE: 为正确匹配查询参数，故在语句后加入空格。
        this.sql = sql + " ";
        this.connection = connection;
        this.databaseNodeConfig = databaseNodeConfig;
        this.persistenceProxy = PersistenceProxy.newInstance(this.databaseNodeConfig);
    }

    AbstractSqlQuery(Connection connection, String sql, DatabaseNodeConfig databaseNodeConfig, Class<?> clazz) {
        // NOTE: 为正确匹配查询参数，故在语句后加入空格。
        this.sql = sql + " ";
        this.connection = connection;
        this.databaseNodeConfig = databaseNodeConfig;
        this.persistenceProxy = PersistenceProxy.newInstance(this.databaseNodeConfig);
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

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
}
