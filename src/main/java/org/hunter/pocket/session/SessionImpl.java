package org.hunter.pocket.session;

import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.criteria.Criteria;
import org.hunter.pocket.criteria.CriteriaImpl;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.exception.SessionException;
import org.hunter.pocket.model.PocketEntity;
import org.hunter.pocket.query.ProcessQuery;
import org.hunter.pocket.query.ProcessQueryImpl;
import org.hunter.pocket.query.SQLQuery;
import org.hunter.pocket.query.SQLQueryImpl;
import org.hunter.pocket.cache.BaseCacheUtils;
import org.hunter.pocket.uuid.UuidGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public int save(PocketEntity entity) {
        Class clazz = entity.getClass();
        Entity entityAnnotation = reflectUtils.getEntityAnnotation(clazz);

        Field[] fields = reflectUtils.getMappingFields(clazz);
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(entityAnnotation.table())
                .append("(")
                .append(reflectUtils.getColumnNames(fields))
                .append(") ");
        StringBuilder valuesSql = new StringBuilder("VALUES(")
                .append(reflectUtils.getColumnPlaceholder(fields))
                .append(") ");
        sql.append(valuesSql);

        this.showSql(sql.toString());
        int effectRow;
        PreparedStatement preparedStatement = null;
        try {
            Serializable uuid = UuidGeneratorFactory.getInstance()
                    .getUuidGenerator(entityAnnotation.uuidGenerator())
                    .getUuid(entity.getClass(), this);
            reflectUtils.setUuidValue(entity, uuid);
            preparedStatement = this.connection.prepareStatement(sql.toString());
            statementApplyValue(entity, fields, preparedStatement);
            effectRow = preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new SessionException(e.getMessage(), e, true, true);
        } finally {
            ConnectionManager.closeIO(preparedStatement, null);
        }
        return effectRow;
    }

    @Override
    public int update(PocketEntity entity) {
        Class clazz = entity.getClass();
        Object older = this.findOne(clazz, reflectUtils.getUuidValue(entity));
        int effectRow = 0;
        if (older != null) {
            Field[] fields = reflectUtils.dirtyFieldFilter(entity, older);
            if (fields.length > 0) {
                StringBuilder sql = new StringBuilder("UPDATE ").append(reflectUtils.getEntityAnnotation(clazz).table()).append(" SET ");
                Column column;
                Join join;
                ManyToOne manyToOne;
                String columnName;
                for (int index = 0; index < fields.length; index++) {
                    column = fields[index].getAnnotation(Column.class);
                    join = fields[index].getAnnotation(Join.class);
                    manyToOne = fields[index].getAnnotation(ManyToOne.class);
                    if (column != null) {
                        columnName = column.name();
                    } else if (join != null) {
                        columnName = join.columnName();
                    } else if (manyToOne != null) {
                        columnName = manyToOne.columnName();
                    } else {
                        throw new SessionException("未找到注解。");
                    }
                    if (index < fields.length - 1) {
                        sql.append(columnName).append(" = ?, ");
                    } else {
                        sql.append(columnName).append(" = ? ");
                    }
                }
                sql.append(" WHERE UUID = ?");
                this.showSql(sql.toString());
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = this.connection.prepareStatement(sql.toString());
                    statementApplyValue(entity, fields, preparedStatement);
                    preparedStatement.setObject(fields.length + 1, reflectUtils.getUuidValue(entity));
                    effectRow = preparedStatement.executeUpdate();
                } catch (Exception e) {
                    throw new SessionException(e.getMessage(), e, true, true);
                } finally {
                    ConnectionManager.closeIO(preparedStatement, null);
                }
            }
            this.removeCache(entity);
        }
        return effectRow;
    }

    @Override
    public int delete(PocketEntity entity) {
        Class clazz = entity.getClass();
        Serializable uuid = reflectUtils.getUuidValue(entity);
        Object garbage = this.findOne(clazz, uuid);
        int effectRow = 0;
        if (garbage != null) {
            String sql = "DELETE FROM " + reflectUtils.getEntityAnnotation(clazz).table() + " WHERE UUID = ?";
            this.showSql(sql);
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = this.connection.prepareStatement(sql);
                preparedStatement.setObject(1, uuid);
                effectRow = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new SessionException(e.getMessage(), e, true, true);
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
                this.baseCacheUtils.set(cacheKey, result, 360L);
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
    public long getMaxUuid(Integer serverId, Class clazz) throws Exception {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT MAX(CONVERT(UUID,SIGNED)) FROM " + annotation.table() + " WHERE UUID REGEXP '^" + serverId + annotation.tableId() + "'");
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
    public void removeCache(PocketEntity entity) {
        String cacheKey = this.baseCacheUtils.generateKey(this.sessionName, entity.getClass(), reflectUtils.getUuidValue(entity));
        this.baseCacheUtils.delete(cacheKey);
    }
}
