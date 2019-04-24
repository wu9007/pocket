package org.hunter.pocket.session;

import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.model.BaseEntity;
import org.hunter.pocket.model.MapperFactory;
import org.hunter.pocket.cache.BaseCacheUtils;
import org.hunter.pocket.utils.ReflectUtils;
import org.hunter.pocket.uuid.UuidGeneratorFactory;
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
    private  static final String SET_UUID_LOCK = "setUUidLock";

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

    /**
     * 保存数据
     *
     * @param entity  entity
     * @param notNull 保存时属性值是否不可为空（true：NULL 不进行保存  false:NULL 同样进行保存）
     * @return effect row number
     * @throws SQLException sql exception
     */
    int saveEntity(BaseEntity entity, boolean notNull) throws SQLException {
        Class clazz = entity.getClass();

        int effectRow;
        PreparedStatement preparedStatement = null;
        try {
            String uuid = UuidGeneratorFactory.getInstance()
                    .getUuidGenerator(MapperFactory.getUuidGenerator(clazz.getName()))
                    .getUuid(entity.getClass(), this);
            synchronized (SET_UUID_LOCK) {
                entity.setUuid(uuid);
                String sql = notNull ? this.buildSaveSqlNotNull(entity) : this.buildSaveSqlNullable(entity);
                this.showSql(sql);
                preparedStatement = this.connection.prepareStatement(sql);
                if (notNull) {
                    this.statementApplyNotNull(entity, preparedStatement);
                } else {
                    this.statementApplyNullable(entity, preparedStatement);
                }
            }
            effectRow = preparedStatement.executeUpdate();
        } finally {
            ConnectionManager.closeIO(preparedStatement, null);
        }
        return effectRow;
    }

    /**
     * prepared Statement 参数赋值
     *
     * @param fields            field array
     * @param entity            entity
     * @param preparedStatement prepared statement
     */
    void statementApply(Field[] fields, BaseEntity entity, PreparedStatement preparedStatement) {
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

    /**
     * 生成保存语句 纳入空值
     *
     * @param entity entity
     * @return sql
     */
    private String buildSaveSqlNullable(BaseEntity entity) {
        return this.buildSaveSql(entity, false);
    }

    /**
     * 生成保存语句 不纳入空值
     *
     * @param entity entity
     * @return sql
     */
    private String buildSaveSqlNotNull(BaseEntity entity) {
        return this.buildSaveSql(entity, true);
    }

    /**
     * prepared Statement 参数赋值（无论属性值是否为空，都进行存储）
     *
     * @param entity            entity
     * @param preparedStatement prepared statement
     */
    private void statementApplyNullable(BaseEntity entity, PreparedStatement preparedStatement) {
        this.statementApply(entity, preparedStatement, false);
    }

    /**
     * prepared Statement 参数赋值（若属性值为空，则不进行存储）
     *
     * @param entity            entity
     * @param preparedStatement prepared statement
     */
    private void statementApplyNotNull(BaseEntity entity, PreparedStatement preparedStatement) {
        this.statementApply(entity, preparedStatement, true);
    }

    /**
     * 生成保存语句
     *
     * @param entity  entity
     * @param notNull 为空的属性是否纳入保存范围
     * @return sql
     */
    private String buildSaveSql(BaseEntity entity, boolean notNull) {
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
    private void statementApply(BaseEntity entity, PreparedStatement preparedStatement, boolean notNull) {
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

    private Stream<Field> validFieldStream(BaseEntity entity) {
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
