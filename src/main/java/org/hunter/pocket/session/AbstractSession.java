package org.hunter.pocket.session;

import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.model.PocketEntity;
import org.hunter.pocket.cache.BaseCacheUtils;
import org.hunter.pocket.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * @author wujianchuan 2019/1/9
 */
abstract class AbstractSession implements Session {

    private final Logger logger = LoggerFactory.getLogger(AbstractSession.class);

    final DatabaseNodeConfig databaseNodeConfig;
    final String sessionName;
    volatile Connection connection;
    Transaction transaction;
    final BaseCacheUtils baseCacheUtils;
    final ReflectUtils reflectUtils = ReflectUtils.getInstance();
    private volatile Boolean closed = true;

    AbstractSession(DatabaseNodeConfig databaseNodeConfig, String sessionName, BaseCacheUtils baseCacheUtils) {
        this.databaseNodeConfig = databaseNodeConfig;
        this.sessionName = sessionName;
        this.baseCacheUtils = baseCacheUtils;
    }

    @Override
    public boolean getClosed() {
        return this.closed;
    }

    void setClosed(boolean closed) {
        this.closed = closed;
    }

    void showSql(String sql) {
        if (this.databaseNodeConfig.getShowSql()) {
            this.logger.info("SQL: {}", sql);
        }
    }

    void statementApplyValue(PocketEntity entity, Field[] fields, PreparedStatement preparedStatement) throws Exception {
        for (int valueIndex = 0; valueIndex < fields.length; valueIndex++) {
            Field field = fields[valueIndex];
            field.setAccessible(true);
            preparedStatement.setObject(valueIndex + 1, field.get(entity));
        }
    }

    String buildSaveSql(Entity entityAnnotation, Field[] fields) {
        List<String> columns = reflectUtils.getColumnNames(fields);
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(entityAnnotation.table())
                .append("(")
                .append(String.join(CommonSql.COMMA, columns))
                .append(") ");
        columns.replaceAll(column -> CommonSql.PLACEHOLDER);
        StringBuilder valuesSql = new StringBuilder("VALUES(")
                .append(String.join(CommonSql.COMMA, columns))
                .append(") ");
        sql.append(valuesSql);
        return sql.toString();
    }
}
