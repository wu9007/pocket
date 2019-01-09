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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

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
        return MYSQL_DB_NAME;
    }

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }

    @Override
    public BaseEntity save(BaseEntity entity) throws Exception {
        Class clazz = entity.getClass();
        String tableName = this.getTableName(clazz);

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(UUID, ");
        StringBuilder valuesSql = new StringBuilder("VALUES(?, ");
        Field[] fields = Arrays.stream(clazz.getDeclaredFields()).filter(NO_MAPPING_FILTER).toArray(Field[]::new);
        for (int index = 0; index < fields.length; index++) {
            if (index < fields.length - 1) {
                sql.append(fields[index].getAnnotation(Column.class).name()).append(", ");
                valuesSql.append("?").append(", ");
            } else {
                sql.append(fields[index].getAnnotation(Column.class).name()).append(") ");
                valuesSql.append("?").append(")");
            }
        }
        sql.append(valuesSql);
        //TODO: 日志收集打印
        System.out.println(sql);
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
        long uuid = HomoUuidGenerator.getInstance().getUuid(entity.getClass(), this);
        entity.setUuid(uuid);
        preparedStatement.setObject(1, uuid);
        for (int valueIndex = 0; valueIndex < fields.length; valueIndex++) {
            Field field = fields[valueIndex];
            field.setAccessible(true);
            preparedStatement.setObject(valueIndex + 2, field.get(entity));
        }
        preparedStatement.executeUpdate();
        preparedStatement.close();
        return entity;
    }

    @Override
    public BaseEntity update(BaseEntity entity) throws Exception {
        Class clazz = entity.getClass();
        String tableName = this.getTableName(clazz);
        BaseEntity older = this.findOne(clazz, entity.getUuid());
        if (older != null) {
            Field[] fields = this.dirtyFieldFilter(entity, older);
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
                //TODO: 日志收集打印
                System.out.println(sql);
                PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
                for (int valueIndex = 0; valueIndex < fields.length; valueIndex++) {
                    Field field = fields[valueIndex];
                    field.setAccessible(true);
                    preparedStatement.setObject(valueIndex + 1, field.get(entity));
                }
                preparedStatement.setObject(fields.length + 1, entity.getUuid());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } else {
                //TODO: 封装异常类型
                throw new RuntimeException("数据未发生变化");
            }
            return entity;
        } else {
            //TODO: 封装异常类型
            throw new RuntimeException("未找到数据");
        }
    }

    @Override
    public int delete(BaseEntity entity) throws Exception {
        Class clazz = entity.getClass();
        String tableName = this.getTableName(clazz);
        BaseEntity garbage = this.findOne(clazz, entity.getUuid());
        if (garbage != null) {
            String sql = "DELETE FROM " + tableName + " WHERE UUID = ?";
            //TODO: 日志收集打印
            System.out.println(sql);
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
    @Cacheable(value = "inventory", key = "#clazz.getName()+#uuid")
    public BaseEntity findOne(Class clazz, Long uuid) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT UUID, ");
        Field[] fields = Arrays.stream(clazz.getDeclaredFields()).filter(NO_MAPPING_FILTER).toArray(Field[]::new);
        for (int index = 0; index < fields.length; index++) {
            sql.append(fields[index].getAnnotation(Column.class).name());
            if (index < fields.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(" FROM ").append(this.getTableName(clazz))
                .append(" WHERE UUID = ?");
        //TODO: 日志收集打印
        System.out.println(sql);
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
        preparedStatement.setLong(1, uuid);
        ResultSet resultSet = preparedStatement.executeQuery();
        BaseEntity entity = (BaseEntity) clazz.newInstance();
        entity.setUuid(uuid);
        if (resultSet.next()) {
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(entity, fieldTypeStrategy.getColumnValue(field, resultSet));
            }
            resultSet.close();
            preparedStatement.close();
            return entity;
        } else {
            resultSet.close();
            preparedStatement.close();
            return null;
        }

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

    private String getTableName(Class clazz) {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        return annotation.table();
    }

    private Field[] dirtyFieldFilter(BaseEntity modern, BaseEntity older) {
        Field[] fields = modern.getClass().getDeclaredFields();
        return Arrays.stream(fields)
                .filter(NO_MAPPING_FILTER)
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        Object modernValue = field.get(modern);
                        Object olderValue = field.get(older);
                        return modernValue == null && olderValue != null || olderValue == null && modernValue != null || modernValue != null && !modernValue.equals(olderValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .toArray(Field[]::new);
    }
}
