package org.homo.dbconnect.inventory;

import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.annotation.Column;
import org.homo.dbconnect.annotation.Entity;
import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.criteria.Criteria;
import org.homo.dbconnect.criteria.CriteriaImpl;
import org.homo.dbconnect.criteria.Restrictions;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;
import org.homo.dbconnect.query.HomoQuery;
import org.homo.dbconnect.uuidstrategy.HomoUuidGenerator;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author wujianchuan 2019/1/1
 */
public class SessionImpl extends AbstractSession {

    SessionImpl(AbstractDatabaseConfig databaseConfig) {
        super(databaseConfig);
    }

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }

    @Override
    public AbstractQuery createSQLQuery(String sql) {
        return new HomoQuery(sql, this.transaction.getConnection());
    }

    @Override
    public Criteria creatCriteria(Class clazz) {
        return new CriteriaImpl(clazz, this.transaction, this.databaseConfig);
    }

    @Override
    public BaseEntity save(BaseEntity entity) throws Exception {
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
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
        statementApplyValue(entity, fields, preparedStatement);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        this.adoptChildren(entity);
        return this.findOne(entity.getClass(), entity.getUuid());
    }

    @Override
    public BaseEntity update(BaseEntity entity) throws Exception {
        Class clazz = entity.getClass();
        String tableName = reflectUtils.getTableName(clazz);
        BaseEntity older = this.findOne(clazz, entity.getUuid());
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
                PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
                statementApplyValue(entity, fields, preparedStatement);
                preparedStatement.setObject(fields.length + 1, entity.getUuid());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } else {
                //TODO: 封装异常类型
                throw new RuntimeException("数据未发生变化");
            }
            return this.findOne(entity.getClass(), entity.getUuid());
        } else {
            //TODO: 封装异常类型
            throw new RuntimeException("未找到数据");
        }
    }

    @Override
    public int delete(BaseEntity entity) throws Exception {
        Class clazz = entity.getClass();
        String tableName = reflectUtils.getTableName(clazz);
        BaseEntity garbage = this.findOne(clazz, entity.getUuid());
        if (garbage != null) {
            String sql = "DELETE FROM " + tableName + " WHERE UUID = ?";
            this.showSql(sql);
            PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql);
            preparedStatement.setLong(1, entity.getUuid());
            int effectRow = preparedStatement.executeUpdate();
            preparedStatement.close();
            return effectRow;
        } else {
            //TODO: 封装异常类型
            throw new RuntimeException("未找到数据");
        }
    }

    @Override
    public BaseEntity findOne(Class clazz, Long uuid) throws Exception {
        Criteria criteria = this.creatCriteria(clazz);
        criteria.add(Restrictions.equ("uuid", uuid));
        return criteria.unique();
    }

    @Override
    public long getMaxUuid(Class clazz) throws Exception {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement("SELECT MAX(UUID) FROM " + annotation.table());
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
