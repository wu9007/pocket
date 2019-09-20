package org.hv.pocket.query;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.CommonSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/3
 */
abstract class AbstractSQLQuery {
    private final Logger logger = LoggerFactory.getLogger(AbstractSQLQuery.class);
    final Connection connection;
    private final DatabaseNodeConfig databaseNodeConfig;
    final String sql;
    private Integer start;
    private Integer limit;
    Class clazz;
    final Map<String, Object> parameterMap = new HashMap<>();
    final List<String> columnNameList = new LinkedList<>();

    AbstractSQLQuery(String sql, Connection connection, DatabaseNodeConfig databaseNodeConfig) {
        this.sql = sql;
        this.connection = connection;
        this.databaseNodeConfig = databaseNodeConfig;
    }

    AbstractSQLQuery(Connection connection, String sql, DatabaseNodeConfig databaseNodeConfig, Class clazz) {
        this.sql = sql;
        this.connection = connection;
        this.databaseNodeConfig = databaseNodeConfig;
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

    void showSql() {
        if (this.databaseNodeConfig.getShowSql()) {
            this.logger.info("Pocket: {}", this.limited() ?
                    new StringBuilder(this.sql).append(" LIMIT ")
                            .append(this.getStart())
                            .append(CommonSql.COMMA)
                            .append(this.getLimit())
                    : this.sql);
        }
    }
}
