package org.hunter.pocket.session;

import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.criteria.Criteria;
import org.hunter.pocket.criteria.CriteriaImpl;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.exception.SessionException;
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
    private static final String CACHE_LOCK = "CACHE_INTO_MONITOR";
    private static final String CACHE_UNLOCK = "CACHE_ESC_MONITOR";
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
    public int save(BaseEntity entity) throws SQLException {
        return super.saveEntity(entity, false);
    }

    @Override
    public int save(BaseEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = this.save(entity);
        if (cascade) {
            String mainClassName = entity.getClass().getName();
            Field[] fields = MapperFactory.getOneToMayFields(mainClassName);
            if (fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    List details = (List) field.get(entity);
                    String mainFieldName = field.getName();
                    String detailListEntityName = MapperFactory.getDetailClassName(mainClassName, mainFieldName);
                    String upBridgeFiledName = MapperFactory.getManyToOneUpField(detailListEntityName, mainClassName);
                    Field upBridgeField = MapperFactory.getField(mainClassName, upBridgeFiledName);
                    Object upBridgeFieldValue = upBridgeField.get(entity);
                    String downBridgeFieldName = MapperFactory.getOneToMayDownFieldName(mainClassName, mainFieldName);
                    Field downBridgeField = MapperFactory.getField(detailListEntityName, downBridgeFieldName);
                    downBridgeField.setAccessible(true);
                    for (Object detail : details) {
                        downBridgeField.set(detail, upBridgeFieldValue);
                        this.save((BaseEntity) detail, true);
                    }
                    effectRow += details.size();
                }
            }
        }
        return effectRow;
    }

    @Override
    public int saveNotNull(BaseEntity entity) throws SQLException {
        return super.saveEntity(entity, true);
    }

    @Override
    public int update(BaseEntity entity) throws SQLException {
        Class clazz = entity.getClass();
        Object older = this.findOne(clazz, entity.getUuid());
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
                        .append("UUID").append(CommonSql.EQUAL_TO).append(CommonSql.PLACEHOLDER);
                this.showSql(sql.toString());
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = this.connection.prepareStatement(sql.toString());
                    this.statementApply(fields, entity, preparedStatement);
                    preparedStatement.setObject(fields.length + 1, reflectUtils.getUuidValue(entity));
                    effectRow = preparedStatement.executeUpdate();
                } finally {
                    ConnectionManager.closeIO(preparedStatement, null);
                }
                this.removeCache(entity);
            }
        }
        return effectRow;
    }

    @Override
    public int delete(BaseEntity entity) throws SQLException {
        Class clazz = entity.getClass();
        Serializable uuid = reflectUtils.getUuidValue(entity);
        Object garbage = this.findOne(clazz, uuid);
        int effectRow = 0;
        if (garbage != null) {
            String sql = CommonSql.DELETE +
                    CommonSql.FROM +
                    MapperFactory.getTableName(clazz.getName()) +
                    CommonSql.WHERE +
                    "UUID" +
                    CommonSql.EQUAL_TO +
                    CommonSql.PLACEHOLDER;
            this.showSql(sql);
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = this.connection.prepareStatement(sql);
                preparedStatement.setObject(1, uuid);
                effectRow = preparedStatement.executeUpdate();
            } finally {
                ConnectionManager.closeIO(preparedStatement, null);
            }
            this.removeCache(entity);
        }
        return effectRow;
    }

    @Override
    public Object findOne(Class clazz, Serializable uuid) {
        String cacheKey = this.baseCacheUtils.generateKey(this.sessionName, clazz, uuid);
        Object result = this.baseCacheUtils.getObj(cacheKey);
        if (result != null) {
            return result;
        }

        boolean lock = false;
        try {
            lock = this.baseCacheUtils.getMapLock().putIfAbsent(cacheKey, cacheKey) == null;
            if (lock) {
                result = this.findDirect(clazz, uuid);
                this.baseCacheUtils.set(cacheKey, result, 3600 * 24 * 7L);
                synchronized (CACHE_UNLOCK) {
                    CACHE_UNLOCK.notifyAll();
                }
            } else {
                synchronized (CACHE_LOCK) {
                    CACHE_LOCK.wait(10);
                    result = this.findOne(clazz, uuid);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SessionException(e.getMessage(), e, true, true);
        } finally {
            if (lock) {
                this.baseCacheUtils.getMapLock().remove(cacheKey);
            }
        }
        return result;
    }

    @Override
    public Object findDirect(Class clazz, Serializable uuid) {
        Criteria criteria = this.createCriteria(clazz);
        criteria.add(Restrictions.equ("uuid", uuid));
        return criteria.unique(true);
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
    public void removeCache(BaseEntity entity) {
        String cacheKey = this.baseCacheUtils.generateKey(this.sessionName, entity.getClass(), reflectUtils.getUuidValue(entity));
        this.baseCacheUtils.delete(cacheKey);
    }
}
