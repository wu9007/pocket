package org.hunter.pocket.session;

import org.hunter.pocket.model.BaseEntity;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.criteria.Criteria;
import org.hunter.pocket.criteria.CriteriaImpl;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.query.AbstractQuery;
import org.hunter.pocket.query.HomoQuery;
import org.hunter.pocket.utils.CacheUtils;
import org.hunter.pocket.utils.HomoUuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author wujianchuan 2019/1/1
 */
public class SessionImpl extends AbstractSession {
    private Logger logger = LoggerFactory.getLogger(SessionImpl.class);

    SessionImpl(DatabaseNodeConfig databaseNodeConfig, String sessionName, CacheUtils cacheUtils) {
        super(databaseNodeConfig, sessionName, cacheUtils);
    }

    @Override
    public synchronized void open() {
        if (this.connection == null) {
            this.connection = ConnectionManager.getInstance().getConnection(databaseNodeConfig);
        } else {
            this.logger.warn("This session is connected. Please don't try again.");
        }
    }

    @Override
    public synchronized void close() {
        if (this.connection != null) {
            ConnectionManager.getInstance().closeConnection(this.databaseNodeConfig.getNodeName(), this.connection);
            this.transaction = null;
            this.connection = null;
        } else {
            this.logger.warn("This session is closed. Please don't try again.");
        }
    }

    @Override
    public synchronized Transaction getTransaction() {
        if (this.transaction == null) {
            this.transaction = new TransactionImpl(this.connection);
        }
        return this.transaction;
    }

    @Override
    public AbstractQuery createSQLQuery(String sql) {
        return new HomoQuery(sql, this.connection);
    }

    @Override
    public Criteria creatCriteria(Class clazz) {
        return new CriteriaImpl(clazz, this.connection, this.databaseNodeConfig);
    }

    @Override
    public int save(BaseEntity entity) throws Exception {
        Class clazz = entity.getClass();
        String tableName = reflectUtils.getTableName(clazz);

        Field[] fields = reflectUtils.getMappingField(clazz);
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append("(")
                .append(reflectUtils.getColumnNames(fields))
                .append(") ");
        StringBuilder valuesSql = new StringBuilder("VALUES(")
                .append(reflectUtils.getColumnPlaceholder(fields))
                .append(") ");
        sql.append(valuesSql);

        this.showSql(sql.toString());
        long uuid = HomoUuidGenerator.getInstance().getUuid(entity.getClass(), this);
        entity.setUuid(uuid);
        PreparedStatement preparedStatement = this.connection.prepareStatement(sql.toString());
        statementApplyValue(entity, fields, preparedStatement);
        int effectRow = preparedStatement.executeUpdate();
        preparedStatement.close();
        this.adoptChildren(entity);
        return effectRow;
    }

    @Override
    public int update(BaseEntity entity) throws Exception {
        Class clazz = entity.getClass();
        String tableName = reflectUtils.getTableName(clazz);
        Object older = this.findOne(clazz, entity.getUuid());
        int effectRow;
        if (older != null) {
            Field[] fields = reflectUtils.dirtyFieldFilter(entity, older);
            if (fields.length > 0) {
                StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
                for (int index = 0; index < fields.length; index++) {
                    if (index < fields.length - 1) {
                        sql.append(fields[index].getAnnotation(Column.class).name()).append(" = ?, ");
                    } else {
                        sql.append(fields[index].getAnnotation(Column.class).name()).append(" = ? ");
                    }
                }
                sql.append(" WHERE UUID = ?");
                this.showSql(sql.toString());
                PreparedStatement preparedStatement = this.connection.prepareStatement(sql.toString());
                statementApplyValue(entity, fields, preparedStatement);
                preparedStatement.setObject(fields.length + 1, entity.getUuid());
                effectRow = preparedStatement.executeUpdate();
                preparedStatement.close();
            } else {
                //TODO: 封装异常类型
                throw new RuntimeException("数据未发生变化");
            }
            this.removeCache(entity);
            return effectRow;
        } else {
            //TODO: 封装异常类型
            throw new RuntimeException("未找到数据");
        }
    }

    @Override
    public int delete(BaseEntity entity) throws Exception {
        Class clazz = entity.getClass();
        String tableName = reflectUtils.getTableName(clazz);
        Object garbage = this.findOne(clazz, entity.getUuid());
        if (garbage != null) {
            String sql = "DELETE FROM " + tableName + " WHERE UUID = ?";
            this.showSql(sql);
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setLong(1, entity.getUuid());
            int effectRow = preparedStatement.executeUpdate();
            preparedStatement.close();
            this.removeCache(entity);
            return effectRow;
        } else {
            //TODO: 封装异常类型
            throw new RuntimeException("未找到数据");
        }
    }

    @Override
    public Object findOne(Class clazz, Long uuid) throws Exception {
        String cacheKey = cacheUtils.generateKey(this.sessionName, clazz, uuid);
        Object result = cacheUtils.getObj(cacheKey);
        if (result != null) {
            return result;
        }

        boolean lock = false;
        try {
            lock = cacheUtils.mapLock.putIfAbsent(cacheKey, cacheKey) == null;
            if (lock) {
                result = this.findDirect(clazz, uuid);
                cacheUtils.set(cacheKey, result, 360L);
            } else {
                this.wait(10);
                result = this.findOne(clazz, uuid);
            }
        } finally {
            if (lock) {
                cacheUtils.mapLock.remove(cacheKey);
            }
        }
        return result;
    }

    @Override
    public Object findDirect(Class clazz, Long uuid) throws Exception {
        Criteria criteria = this.creatCriteria(clazz);
        criteria.add(Restrictions.equ("uuid", uuid));
        return criteria.unique(true);
    }

    @Override
    public long getMaxUuid(Class clazz) throws Exception {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT MAX(UUID) FROM " + annotation.table());
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
        String cacheKey = this.cacheUtils.generateKey(this.sessionName, entity.getClass(), entity.getUuid());
        this.cacheUtils.delete(cacheKey);
    }
}
