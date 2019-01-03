package org.homo.dbconnect.session;

import org.homo.core.annotation.HomoColumn;
import org.homo.core.annotation.HomoEntity;
import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.transaction.HomoTransaction;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;
import org.homo.dbconnect.query.HomoQuery;
import org.homo.dbconnect.uuidstrategy.HomoUuidGenerator;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
public class HomoSession implements Session {

    private Connection connection;
    private FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();

    HomoSession(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Transaction getTransaction() {
        return new HomoTransaction(this.connection);
    }

    @Override
    public int save(BaseEntity entity) throws SQLException, IllegalAccessException {
        Class clazz = entity.getClass();
        String tableName = this.getTableName(clazz);

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(UUID, ");
        StringBuilder valuesSql = new StringBuilder("VALUES(?, ");
        Field[] fields = clazz.getDeclaredFields();
        for (int index = 1; index < fields.length; index++) {
            if (index < fields.length - 1) {
                sql.append(fields[index].getAnnotation(HomoColumn.class).name()).append(", ");
                valuesSql.append("?").append(", ");
            } else {
                sql.append(fields[index].getAnnotation(HomoColumn.class).name()).append(") ");
                valuesSql.append("?").append(")");
            }
        }
        sql.append(valuesSql);

        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        preparedStatement.setObject(1, HomoUuidGenerator.getInstance().getUuid(entity.getClass(), this));
        for (int valueIndex = 1; valueIndex < fields.length; valueIndex++) {
            Field field = fields[valueIndex];
            field.setAccessible(true);
            preparedStatement.setObject(valueIndex + 1, field.get(entity));
        }
        return preparedStatement.executeUpdate();
    }

    @Override
    public int update(BaseEntity entity) {
        return 0;
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
            sql.append(fields[index].getAnnotation(HomoColumn.class).name());
            if (index < fields.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(" FROM ").append(this.getTableName(clazz))
                .append(" WHERE UUID = ?");
        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        preparedStatement.setLong(1, uuid);
        ResultSet resultSet = preparedStatement.executeQuery();
        BaseEntity entity = (BaseEntity) clazz.newInstance();
        entity.setUuid(uuid);
        if (resultSet.next()) {
            for (int index = 1; index < fields.length; index++) {
                Field field = fields[index];
                String columnName = field.getAnnotation(HomoColumn.class).name();
                field.setAccessible(true);
                field.set(entity, fieldTypeStrategy.getColumnValue(field, resultSet));
            }
        }
        return entity;
    }

    @Override
    public AbstractQuery createSQLQuery(String sql) {
        return new HomoQuery(sql, connection);
    }

    @Override
    public long getMaxUuid(Class clazz) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT MAX(UUID) FROM USER");
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            return 0;
        }
    }

    private String getTableName(Class clazz) {
        HomoEntity annotation = (HomoEntity) clazz.getAnnotation(HomoEntity.class);
        return annotation.table();
    }
}
