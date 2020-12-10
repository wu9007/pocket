package org.hv.pocket.session;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.criteria.PersistenceProxy;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.identify.IdentifyGeneratorFactory;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.utils.EncryptUtil;
import org.hv.pocket.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wujianchuan 2019/1/9
 */
abstract class AbstractSession implements Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSession.class);

    final PersistenceProxy persistenceProxy;
    final DatabaseNodeConfig databaseNodeConfig;
    final String sessionName;
    volatile Connection connection;
    Transaction transaction;
    final ReflectUtils reflectUtils = ReflectUtils.getInstance();
    private volatile Boolean closed = true;

    AbstractSession(DatabaseNodeConfig databaseNodeConfig, String sessionName) {
        this.databaseNodeConfig = databaseNodeConfig;
        this.sessionName = sessionName;
        this.persistenceProxy = PersistenceProxy.newInstance(this.databaseNodeConfig);
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
            Serializable identify = entity.loadIdentify() == null ? IdentifyGeneratorFactory.getInstance()
                    .getIdentifyGenerator(MapperFactory.getIdentifyGenerationType(clazz.getName()))
                    .getIdentify(entity.getClass(), this) : entity.loadIdentify();
            entity.putIdentify(identify);
            String sql = nullAble ? this.buildSaveSqlNullable(entity) : this.buildSaveSqlNotNull(entity);
            preparedStatement = this.connection.prepareStatement(sql);
            LOGGER.debug("Creates a <code>PreparedStatement</code> object");
            if (nullAble) {
                this.statementApplyNullable(entity, preparedStatement);
            } else {
                this.statementApplyNotNull(entity, preparedStatement);
            }
            effectRow = this.persistenceProxy.executeWithLog(preparedStatement, PreparedStatement::executeUpdate, sql);
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
                Class<? extends AbstractEntity> childClass = MapperFactory.getDetailClass(mainClassName, mainFieldName);
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
     * 删除数据，非级联删除
     *
     * @param entity  实例
     * @param cascade 是否进行级联保存操作
     * @return 影响行数
     * @throws SQLException e
     */
    int deleteEntity(AbstractEntity entity, boolean cascade) throws SQLException {
        Class<? extends AbstractEntity> clazz = entity.getClass();
        Serializable identify = entity.loadIdentify();
        String identifyFieldName = MapperFactory.getIdentifyFieldName(clazz.getName());
        Object garbage = this.findOne(clazz, identify, cascade);
        int effectRow = 0;
        if (garbage != null) {
            // delete main data
            Criteria criteria = this.createCriteria(clazz);
            criteria.add(Restrictions.equ(identifyFieldName, identify));
            effectRow += criteria.delete();
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
                String encryptModel = MapperFactory.getEncryptModel(entity.getClass().getName(), field.getName());
                Object fieldValue = field.get(entity);
                // 判断是否需要加密持久化
                if (fieldValue != null && !StringUtils.isEmpty(encryptModel)) {
                    fieldValue = EncryptUtil.encrypt(encryptModel, fieldValue.toString());
                }
                if (fieldValue instanceof LocalDate) {
                    fieldValue = java.sql.Date.valueOf((LocalDate) fieldValue);
                } else if (fieldValue instanceof LocalDateTime) {
                    fieldValue = java.sql.Timestamp.valueOf((LocalDateTime) fieldValue);
                } else if (fieldValue instanceof Date) {
                    fieldValue = new java.sql.Date(((Date) fieldValue).getTime());
                }
                preparedStatement.setObject(valueIndex + 1, fieldValue);
            } catch (SQLException | IllegalAccessException e) {
                LOGGER.error(e.getMessage());
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
        String valuesSql = CommonSql.VALUES +
                CommonSql.LEFT_BRACKET +
                String.join(CommonSql.COMMA, columns) +
                CommonSql.RIGHT_BRACKET;
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
                        LOGGER.warn(e.getMessage());
                        return false;
                    }
                });
    }
}
