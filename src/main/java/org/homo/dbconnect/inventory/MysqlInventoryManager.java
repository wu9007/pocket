package org.homo.dbconnect.inventory;

import org.homo.core.annotation.Column;
import org.homo.core.annotation.Entity;
import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.DatabaseManager;
import org.homo.dbconnect.transaction.HomoTransaction;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;
import org.homo.dbconnect.query.HomoQuery;
import org.homo.dbconnect.uuidstrategy.HomoUuidGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
@Component
public class MysqlInventoryManager implements InventoryManager {

    private Transaction transaction;
    private FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();

    MysqlInventoryManager(DatabaseManager databaseManager) {
        this.transaction = new HomoTransaction(databaseManager);
    }

    @Override
    public String getDbName() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }

    @Override
    public BaseEntity save(BaseEntity entity) throws SQLException, IllegalAccessException {
        Class clazz = entity.getClass();
        String tableName = this.getTableName(clazz);

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(UUID, ");
        StringBuilder valuesSql = new StringBuilder("VALUES(?, ");
        Field[] fields = clazz.getDeclaredFields();
        for (int index = 1; index < fields.length; index++) {
            if (index < fields.length - 1) {
                sql.append(fields[index].getAnnotation(Column.class).name()).append(", ");
                valuesSql.append("?").append(", ");
            } else {
                sql.append(fields[index].getAnnotation(Column.class).name()).append(") ");
                valuesSql.append("?").append(")");
            }
        }
        sql.append(valuesSql);

        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
        long uuid = HomoUuidGenerator.getInstance().getUuid(entity.getClass(), this);
        entity.setUuid(uuid);
        preparedStatement.setObject(1, uuid);
        for (int valueIndex = 1; valueIndex < fields.length; valueIndex++) {
            Field field = fields[valueIndex];
            field.setAccessible(true);
            preparedStatement.setObject(valueIndex + 1, field.get(entity));
        }
        preparedStatement.executeUpdate();
        return entity;
    }

    @Override
    public BaseEntity update(BaseEntity entity) {
        return entity;
    }

    @Override
    public int delete(BaseEntity entity) {
        return 0;
    }

    @Override
    public BaseEntity findOne(Class clazz, Long uuid) throws SQLException, IllegalAccessException, InstantiationException {
        StringBuilder sql = new StringBuilder("SELECT UUID, ");
        Field[] fields = clazz.getDeclaredFields();
        for (int index = 1; index < fields.length; index++) {
            sql.append(fields[index].getAnnotation(Column.class).name());
            if (index < fields.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(" FROM ").append(this.getTableName(clazz))
                .append(" WHERE UUID = ?");
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
        preparedStatement.setLong(1, uuid);
        ResultSet resultSet = preparedStatement.executeQuery();
        BaseEntity entity = (BaseEntity) clazz.newInstance();
        entity.setUuid(uuid);
        if (resultSet.next()) {
            for (int index = 1; index < fields.length; index++) {
                Field field = fields[index];
                field.setAccessible(true);
                field.set(entity, fieldTypeStrategy.getColumnValue(field, resultSet));
            }
        }
        return entity;
    }

    @Override
    public AbstractQuery createSQLQuery(String sql) {
        return new HomoQuery(sql, this.transaction.getConnection());
    }

    @Override
    public long getMaxUuid(Class clazz) throws SQLException {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement("SELECT MAX(UUID) FROM " + annotation.table());
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            return 0;
        }
    }

    private String getTableName(Class clazz) {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        return annotation.table();
    }
}
