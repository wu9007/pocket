package org.hv.pocket.session;

import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.annotation.Entity;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.criteria.CriteriaImpl;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.exception.SessionException;
import org.hv.pocket.model.DetailInductiveBox;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.model.BaseEntity;
import org.hv.pocket.query.ProcessQuery;
import org.hv.pocket.query.ProcessQueryImpl;
import org.hv.pocket.query.SQLQuery;
import org.hv.pocket.query.SQLQueryImpl;
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
    private static final String CACHE_UNLOCK = "CACHE_UNLOCK";
    private static final String CACHE_LOCK = "CACHE_LOCK";
    private static final String IDENTIFICATION = "UUID";
    private static final String OPEN_LOCK = "OPEN_MONITOR";
    private static final String CLOSE_LOCK = "CLOSE_MONITOR";
    private static final String TRANSACTION_LOCK = "TRANSACTION_MONITOR";

    SessionImpl(DatabaseNodeConfig databaseNodeConfig, String sessionName) {
        super(databaseNodeConfig, sessionName);
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
        return new SQLQueryImpl(sql, this.connection, this.databaseNodeConfig);
    }

    @Override
    public SQLQuery createSQLQuery(String sql, Class clazz) {
        return new SQLQueryImpl(sql, connection, this.databaseNodeConfig, clazz);
    }

    @Override
    public <T> ProcessQuery<T> createProcessQuery(String processSQL) {
        return new ProcessQueryImpl<>(processSQL, this.connection, this.databaseNodeConfig);
    }

    @Override
    public Criteria createCriteria(Class clazz) {
        return new CriteriaImpl(clazz, this);
    }

    @Override
    public Object findOne(Class clazz, Serializable uuid) {
        String cacheKey = this.cache.generateKey(clazz, uuid);
        Object result = this.cache.get(cacheKey);
        if (result != null) {
            return result;
        }

        boolean lock = false;
        try {
            lock = this.cache.getMapLock().putIfAbsent(cacheKey, cacheKey) == null;
            if (lock) {
                result = this.findDirect(clazz, uuid);
                this.cache.set(cacheKey, result);
                synchronized (CACHE_UNLOCK) {
                    CACHE_UNLOCK.notifyAll();
                }
            } else {
                synchronized (CACHE_LOCK) {
                    CACHE_LOCK.wait(10);
                    result = this.findOne(clazz, uuid);
                }
            }
        } catch (InterruptedException | SQLException e) {
            Thread.currentThread().interrupt();
            throw new SessionException(e.getMessage(), e, true, true);
        } finally {
            if (lock) {
                this.cache.getMapLock().remove(cacheKey);
            }
        }
        return result;
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
                    ConnectionManager.closeIo(preparedStatement, null);
                }
            }
            String key = this.cache.generateKey(clazz, entity.getUuid());
            this.cache.set(key, entity);
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
                    field.setAccessible(true);
                    List<BaseEntity> details = (List<BaseEntity>) field.get(entity);
                    if (details != null) {
                        for (BaseEntity detail : details) {
                            effectRow += this.delete(detail);
                        }
                    }
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
                String key = this.cache.generateKey(clazz, uuid);
                this.cache.remove(key);
            } finally {
                ConnectionManager.closeIo(preparedStatement, null);
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

    @Override
    public CacheHolder getCacheHolder() {
        return this.cache;
    }
}
