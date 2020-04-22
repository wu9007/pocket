package org.hv.pocket.session;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.logger.StatementProxy;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.utils.ReflectUtils;
import org.hv.pocket.uuid.UuidGeneratorFactory;

import java.io.Serializable;
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
    private static final String SET_IDENTIFY_LOCK = "setIdentifyLock";

    final StatementProxy statementProxy;
    final DatabaseNodeConfig databaseNodeConfig;
    final String sessionName;
    volatile Connection connection;
    Transaction transaction;
    final ReflectUtils reflectUtils = ReflectUtils.getInstance();
    private volatile Boolean closed = true;

    AbstractSession(DatabaseNodeConfig databaseNodeConfig, String sessionName) {
        this.databaseNodeConfig = databaseNodeConfig;
        this.sessionName = sessionName;
        this.statementProxy = StatementProxy.newInstance(this.databaseNodeConfig);
    }

    @Override
    public DatabaseNodeConfig getDatabaseNodeConfig() {
        return databaseNodeConfig;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String getSessionName() {
        return this.sessionName;
    }

    @Override
    public boolean getClosed() {
        return this.closed;
    }

    void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * 保存数据
     *
     * @param entity   entity
     * @param nullAble 保存时属性值是否不可为空（false：NULL 不进行保存  true:NULL 同样进行保存）
     * @return effect row number
     * @throws SQLException sql exception
     */
    int saveEntity(AbstractEntity entity, boolean nullAble) throws SQLException {
        Class<?> clazz = entity.getClass();

        int effectRow;
        PreparedStatement preparedStatement = null;
        try {
            Serializable uuid = entity.getIdentify() == null ? UuidGeneratorFactory.getInstance()
                    .getUuidGenerator(MapperFactory.getUuidGenerationType(clazz.getName()))
                    .getIdentify(entity.getClass(), this) : entity.getIdentify();
            synchronized (SET_IDENTIFY_LOCK) {
                entity.setIdentify(uuid);
                String sql = nullAble ? this.buildSaveSqlNullable(entity) : this.buildSaveSqlNotNull(entity);
                preparedStatement = this.connection.prepareStatement(sql);
                if (nullAble) {
                    this.statementApplyNullable(entity, preparedStatement);
                } else {
                    this.statementApplyNotNull(entity, preparedStatement);
                }
            }
            effectRow = this.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeUpdate);
        } finally {
            ConnectionManager.closeIo(preparedStatement, null);
        }
        return effectRow;
    }

    /**
     * 级联保存明细
     *
     * @param entity   entity
     * @param nullAble 保存时属性值是否不可为空（false：NULL 不进行保存  true:NULL 同样进行保存）
     * @return effect row
     * @throws IllegalAccessException e
     * @throws SQLException           e
     */
    int saveDetails(AbstractEntity entity, boolean nullAble) throws IllegalAccessException, SQLException {
        int effectRow = 0;
        String mainClassName = entity.getClass().getName();
        Field[] fields = MapperFactory.getOneToMayFields(mainClassName);
        if (fields.length > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                List<?> details = (List<?>) field.get(entity);
                String mainFieldName = field.getName();
                Class<?> childClass = MapperFactory.getDetailClass(mainClassName, mainFieldName);
                String downBridgeFieldName = MapperFactory.getOneToMayDownFieldName(mainClassName, mainFieldName);
                Field downBridgeField = MapperFactory.getField(childClass.getName(), downBridgeFieldName);
                Object upBridgeFieldValue = MapperFactory.getUpBridgeFieldValue(entity, mainClassName, childClass);
                downBridgeField.setAccessible(true);
                if (details != null) {
                    for (Object detail : details) {
                        downBridgeField.set(detail, upBridgeFieldValue);
                        this.saveEntity((AbstractEntity) detail, nullAble);
                        this.saveDetails((AbstractEntity) detail, nullAble);
                    }
                    effectRow += details.size();
                }
            }
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
    void statementApply(Field[] fields, AbstractEntity entity, PreparedStatement preparedStatement) {
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
    private String buildSaveSqlNullable(AbstractEntity entity) {
        return this.buildSaveSql(entity, false);
    }

    /**
     * 生成保存语句 不纳入空值
     *
     * @param entity entity
     * @return sql
     */
    private String buildSaveSqlNotNull(AbstractEntity entity) {
        return this.buildSaveSql(entity, true);
    }

    /**
     * prepared Statement 参数赋值（无论属性值是否为空，都进行存储）
     *
     * @param entity            entity
     * @param preparedStatement prepared statement
     */
    private void statementApplyNullable(AbstractEntity entity, PreparedStatement preparedStatement) {
        this.statementApply(entity, preparedStatement, false);
    }

    /**
     * prepared Statement 参数赋值（若属性值为空，则不进行存储）
     *
     * @param entity            entity
     * @param preparedStatement prepared statement
     */
    private void statementApplyNotNull(AbstractEntity entity, PreparedStatement preparedStatement) {
        this.statementApply(entity, preparedStatement, true);
    }

    /**
     * 生成保存语句
     *
     * @param entity  entity
     * @param notNull 为空的属性是否纳入保存范围
     * @return sql
     */
    private String buildSaveSql(AbstractEntity entity, boolean notNull) {
        Class<?> clazz = entity.getClass();
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
    private void statementApply(AbstractEntity entity, PreparedStatement preparedStatement, boolean notNull) {
        Class<?> clazz = entity.getClass();
        Field[] fields;
        if (notNull) {
            fields = this.validFieldStream(entity)
                    .toArray(Field[]::new);
        } else {
            fields = MapperFactory.getRepositoryFields(clazz.getName());
        }
        this.statementApply(fields, entity, preparedStatement);
    }

    private Stream<Field> validFieldStream(AbstractEntity entity) {
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
