package org.hunter.pocket.session;

import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.model.MapperFactory;
import org.hunter.pocket.model.PocketEntity;
import org.hunter.pocket.cache.BaseCacheUtils;
import org.hunter.pocket.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    String buildSaveSqlNullable(PocketEntity entity) {
        return this.buildSaveSql(entity, false);
    }

    String buildSaveSqlNotNull(PocketEntity entity) {
        return this.buildSaveSql(entity, true);
    }

    void statementApplyNullable(PocketEntity entity, PreparedStatement preparedStatement) {
        this.statementApply(entity, preparedStatement, false);
    }

    void statementApplyNotNull(PocketEntity entity, PreparedStatement preparedStatement) {
        this.statementApply(entity, preparedStatement, true);
    }

    /**
     * 生成保存语句
     *
     * @param entity  entity
     * @param notNull 为空的属性是否纳入保存范围
     * @return sql
     */
    private String buildSaveSql(PocketEntity entity, boolean notNull) {
        Class clazz = entity.getClass();
        List<String> columns;
        if (notNull) {
            columns = this.validFieldStream(entity)
                    .map(field -> MapperFactory.getRepositoryColumnName(clazz.getName(), field.getName()))
                    .collect(Collectors.toList());
        } else {
            columns = new LinkedList<>(MapperFactory.getRepositoryColumnNames(clazz.getName()));
        }
        StringBuilder sql = new StringBuilder(CommonSql.INSERT_INTO)
                .append(MapperFactory.getTableName(clazz.getName()))
                .append(CommonSql.LEFT_BRACKET)
                .append(String.join(CommonSql.COMMA, columns))
                .append(CommonSql.RIGHT_BRACKET);
        columns.replaceAll(column -> CommonSql.PLACEHOLDER);
        StringBuilder valuesSql = new StringBuilder(CommonSql.VALUES)
                .append(CommonSql.LEFT_BRACKET)
                .append(String.join(CommonSql.COMMA, columns))
                .append(CommonSql.RIGHT_BRACKET);
        sql.append(valuesSql);
        return sql.toString();
    }

    /**
     * @param entity            entity
     * @param preparedStatement prepared statement
     * @param notNull           为空的属性是否纳入保存范围
     */
    private void statementApply(PocketEntity entity, PreparedStatement preparedStatement, boolean notNull) {
        Class clazz = entity.getClass();
        Field[] fields;
        if (notNull) {
            fields = this.validFieldStream(entity)
                    .toArray(Field[]::new);
        } else {
            fields = MapperFactory.getRepositoryFields(clazz.getName());
        }
        this.statementApply(fields, entity, preparedStatement);
    }

    /**
     * 查询参数赋值
     *
     * @param fields            field array
     * @param entity            entity
     * @param preparedStatement prepared statement
     */
    void statementApply(Field[] fields, PocketEntity entity, PreparedStatement preparedStatement) {
        for (int valueIndex = 0; valueIndex < fields.length; valueIndex++) {
            Field field = fields[valueIndex];
            field.setAccessible(true);
            try {
                preparedStatement.setObject(valueIndex + 1, field.get(entity));
            } catch (SQLException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Stream<Field> validFieldStream(PocketEntity entity) {
        return Arrays.stream(MapperFactory.getRepositoryFields(entity.getClass().getName()))
                .filter(field -> {
                    field.setAccessible(true);
                    try {
                        return field.get(entity) != null;
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                });
    }
}
