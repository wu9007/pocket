package org.hunter.pocket.session;

import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.criteria.Criteria;
import org.hunter.pocket.criteria.CriteriaImpl;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.model.DetailInductiveBox;
import org.hunter.pocket.model.MapperFactory;
import org.hunter.pocket.model.BaseEntity;
import org.hunter.pocket.query.ProcessQuery;
import org.hunter.pocket.query.ProcessQueryImpl;
import org.hunter.pocket.query.SQLQuery;
import org.hunter.pocket.query.SQLQueryImpl;
import org.hunter.pocket.cache.BaseCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wujianchuan 2019/1/1
 */
public class SessionImpl extends AbstractSession {
    private final Logger logger = LoggerFactory.getLogger(SessionImpl.class);
    private static final String IDENTIFICATION = "UUID";
    private static final String OPEN_LOCK = "OPEN_MONITOR";
    private static final String CLOSE_LOCK = "CLOSE_MONITOR";
    private static final String TRANSACTION_LOCK = "TRANSACTION_MONITOR";

    SessionImpl(DatabaseNodeConfig databaseNodeConfig, String sessionName, BaseCacheUtils baseCacheUtils) {
        super(databaseNodeConfig, sessionName, baseCacheUtils);
    }

    @Override
    public void open() {
        if (this.connection == null) {
            synchronized (OPEN_LOCK) {
                if (this.connection == null) {
                    this.connection = ConnectionManager.getInstance().getConnection(databaseNodeConfig);
                    this.setClosed(false);
                    this.logger.info("Session: {} turned on.", this.sessionName);
                } else {
                    this.logger.warn("This session is connected. Please don't try again.");
                }
            }
        } else {
            this.logger.warn("This session is connected. Please don't try again.");
        }
    }

    @Override
    public void close() {
        if (this.connection != null) {
            synchronized (CLOSE_LOCK) {
                if (this.connection != null) {
                    ConnectionManager.getInstance().closeConnection(this.databaseNodeConfig.getNodeName(), this.connection);
                    this.transaction = null;
                    this.connection = null;
                    this.setClosed(true);
                    this.logger.info("Session: {} turned off.", this.sessionName);
                } else {
                    this.logger.warn("This session is closed. Please don't try again.");
                }
            }
        } else {
            this.logger.warn("This session is closed. Please don't try again.");
        }
    }

    @Override
    public Transaction getTransaction() {
        if (this.transaction == null) {
            synchronized (TRANSACTION_LOCK) {
                if (this.transaction == null) {
                    this.transaction = new TransactionImpl(this.connection);
                }
            }
        }
        return this.transaction;
    }

    @Override
    public SQLQuery createSQLQuery(String sql) {
        return new SQLQueryImpl(sql, this.connection);
    }

    @Override
    public SQLQuery createSQLQuery(String sql, Class clazz) {
        return new SQLQueryImpl(sql, connection, clazz);
    }

    @Override
    public <T> ProcessQuery<T> createProcessQuery(String processSQL) {
        return new ProcessQueryImpl<>(processSQL, this.connection);
    }

    @Override
    public Criteria createCriteria(Class clazz) {
        return new CriteriaImpl(clazz, this.connection, this.databaseNodeConfig);
    }

    @Override
    public Object findOne(Class clazz, Serializable uuid) throws SQLException {
        return this.findDirect(clazz, uuid);
    }

    @Override
    public Object findDirect(Class clazz, Serializable uuid) throws SQLException {
        Criteria criteria = this.createCriteria(clazz);
        criteria.add(Restrictions.equ("uuid", uuid));
        return criteria.unique(true);
    }

    @Override
    public int save(BaseEntity entity) throws SQLException {
        return super.saveEntity(entity, true);
    }

    @Override
    public int save(BaseEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = this.save(entity);
        if (cascade) {
            effectRow += super.saveDetails(entity, true);
        }
        return effectRow;
    }

    @Override
    public int shallowSave(BaseEntity entity) throws SQLException {
        return super.saveEntity(entity, false);
    }

    @Override
    public int shallowSave(BaseEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = this.shallowSave(entity);
        if (cascade) {
            effectRow += super.saveDetails(entity, false);
        }
        return effectRow;
    }

    @Override
    public int update(BaseEntity entity) throws SQLException {
        Class clazz = entity.getClass();
        BaseEntity older = (BaseEntity) this.findOne(clazz, entity.getUuid());
        int effectRow = 0;
        if (older != null) {
            Field[] fields = reflectUtils.dirtyFieldFilter(entity, older);
            if (fields.length > 0) {
                StringBuilder sql = new StringBuilder(CommonSql.UPDATE)
                        .append(MapperFactory.getTableName(clazz.getName()))
                        .append(CommonSql.SET);

                List<String> setValues = new LinkedList<>();
                for (Field field : fields) {
                    setValues.add(MapperFactory.getRepositoryColumnName(clazz.getName(), field.getName()) + CommonSql.EQUAL_TO + CommonSql.PLACEHOLDER);
                }
                sql.append(String.join(CommonSql.COMMA, setValues))
                        .append(CommonSql.WHERE)
                        .append(IDENTIFICATION).append(CommonSql.EQUAL_TO).append(CommonSql.PLACEHOLDER);
                this.showSql(sql.toString());
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = this.connection.prepareStatement(sql.toString());
                    this.statementApply(fields, entity, preparedStatement);
                    preparedStatement.setObject(fields.length + 1, entity.getUuid());
                    effectRow = preparedStatement.executeUpdate();
                } finally {
                    ConnectionManager.closeIO(preparedStatement, null);
                }
            }
        }
        return effectRow;
    }

    @Override
    public int update(BaseEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = 0;
        Class clazz = entity.getClass();
        Object older = this.findOne(clazz, entity.getUuid());
        if (cascade) {
            String mainClassName = entity.getClass().getName();
            Field[] fields = MapperFactory.getOneToMayFields(mainClassName);
            if (fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    DetailInductiveBox detailBox = DetailInductiveBox.newInstance(field.get(entity), field.get(older));
                    List<BaseEntity> newbornDetails = detailBox.getNewborn();
                    if (newbornDetails.size() > 0) {
                        Class childrenClass = MapperFactory.getDetailClass(mainClassName, field.getName());
                        String downBridgeFieldName = MapperFactory.getOneToMayDownFieldName(mainClassName, field.getName());
                        Field downBridgeField = MapperFactory.getField(childrenClass.getName(), downBridgeFieldName);
                        Object upBridgeFieldValue = MapperFactory.getUpBridgeFieldValue(entity, mainClassName, childrenClass);
                        downBridgeField.setAccessible(true);
                        for (BaseEntity detail : newbornDetails) {
                            downBridgeField.set(detail, upBridgeFieldValue);
                            this.save(detail, true);
                        }
                    }
                    for (BaseEntity detail : detailBox.getMoribund()) {
                        this.delete(detail);
                    }
                    for (BaseEntity detail : detailBox.getUpdate()) {
                        this.update(detail, true);
                    }
                    effectRow += detailBox.getCount();
                }
            }
        }
        effectRow += this.update(entity);
        return effectRow;
    }

    @Override
    public int delete(BaseEntity entity) throws SQLException, IllegalAccessException {
        Class clazz = entity.getClass();
        String mainClassName = clazz.getName();
        Serializable uuid = entity.getUuid();

        Object garbage = this.findOne(clazz, uuid);
        int effectRow = 0;
        if (garbage != null) {
            // delete detail list data
            Field[] fields = MapperFactory.getOneToMayFields(mainClassName);
            if (fields.length > 0) {
                for (Field field : fields) {
                    String mainFieldName = field.getName();
                    Class childrenClass = MapperFactory.getDetailClass(mainClassName, mainFieldName);
                    String downBridgeFieldName = MapperFactory.getOneToMayDownFieldName(mainClassName, mainFieldName);
                    Object upBridgeFieldValue = MapperFactory.getUpBridgeFieldValue(entity, mainClassName, childrenClass);
                    effectRow += this.createCriteria(childrenClass)
                            .add(Restrictions.equ(downBridgeFieldName, upBridgeFieldValue))
                            .delete();
                }
            }

            // delete main data
            String sql = CommonSql.DELETE +
                    CommonSql.FROM +
                    MapperFactory.getTableName(clazz.getName()) +
                    CommonSql.WHERE +
                    IDENTIFICATION +
                    CommonSql.EQUAL_TO +
                    CommonSql.PLACEHOLDER;
            this.showSql(sql);
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = this.connection.prepareStatement(sql);
                preparedStatement.setObject(1, uuid);
                effectRow += preparedStatement.executeUpdate();
            } finally {
                ConnectionManager.closeIO(preparedStatement, null);
            }
        }
        return effectRow;
    }

    @Override
    public long getMaxUuid(Integer serverId, Class clazz) throws SQLException {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        String sql = CommonSql.SELECT
                + "MAX(CONVERT(UUID,SIGNED))"
                + CommonSql.FROM + annotation.table()
                + CommonSql.WHERE
                + "UUID REGEXP '^" + serverId + annotation.tableId() + "'";
        PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        long uuid;
        if (resultSet.next()) {
            uuid = resultSet.getLong(1);
        } else {
            uuid = 0;
        }
        resultSet.close();
        preparedStatement.close();
        return uuid;
    }
}
