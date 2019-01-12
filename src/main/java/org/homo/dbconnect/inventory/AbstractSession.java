package org.homo.dbconnect.inventory;

import org.homo.core.annotation.Column;
import org.homo.core.annotation.Entity;
import org.homo.core.annotation.ManyToOne;
import org.homo.core.annotation.OneToMany;
import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.utils.ReflectUtils;
import org.homo.dbconnect.uuidstrategy.HomoUuidGenerator;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.homo.dbconnect.utils.ReflectUtils.FIND_CHILDREN;
import static org.homo.dbconnect.utils.ReflectUtils.FIND_PARENT;

/**
 * @author wujianchuan 2019/1/9
 */
abstract class AbstractSession implements Session {

    static final String MYSQL_DB_NAME = "com.mysql.cj.jdbc.Driver";
    static final String ORACLE_DB_NAME = "com.oracle.jdbc.Driver";

    Transaction transaction;
    private FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();
    private ReflectUtils reflectUtils = ReflectUtils.getInstance();

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
        //TODO: 日志收集打印
        System.out.println(sql);
        long uuid = HomoUuidGenerator.getInstance().getUuid(entity.getClass(), this);
        entity.setUuid(uuid);
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
        for (int valueIndex = 0; valueIndex < fields.length; valueIndex++) {
            Field field = fields[valueIndex];
            field.setAccessible(true);
            preparedStatement.setObject(valueIndex + 1, field.get(entity));
        }
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
        String tableName = reflectUtils.getTableName(clazz);
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
        StringBuilder sql = new StringBuilder("SELECT ");
        Field[] fields = reflectUtils.getMappingField(clazz);
        Field[] childrenFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(FIND_CHILDREN)
                .toArray(Field[]::new);
        sql.append(reflectUtils.getColumnNames(fields)).append(" FROM ").append(reflectUtils.getTableName(clazz))
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
                if (field.getAnnotation(Column.class) != null) {
                    field.set(entity, fieldTypeStrategy.getColumnValue(field, resultSet));
                }
            }
            if (childrenFields.length > 0) {
                for (Field childField : childrenFields) {
                    childField.setAccessible(true);
                    if (childField.getAnnotation(OneToMany.class) != null) {
                        childField.set(entity, this.getChildren(childField, uuid));
                    }
                }
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

    private void adoptChildren(BaseEntity entity) throws Exception {
        Field[] childrenFields = Arrays.stream(entity.getClass().getDeclaredFields()).filter(FIND_CHILDREN).toArray(Field[]::new);
        if (childrenFields.length > 0) {
            for (Field childField : childrenFields) {
                childField.setAccessible(true);
                OneToMany oneToMany = childField.getAnnotation(OneToMany.class);
                Collection child = (Collection) childField.get(entity);
                if (child.size() > 0) {
                    Field[] detailFields = childField.getAnnotation(OneToMany.class).clazz().getDeclaredFields();
                    Field mappingField = Arrays.stream(detailFields)
                            .filter(FIND_PARENT)
                            .filter(field -> oneToMany.name().equals(field.getAnnotation(ManyToOne.class).name()))
                            .findFirst().orElseThrow(() -> new NullPointerException("子表实体未配置ManyToOne(name = \"" + oneToMany.name() + "\")注解"));
                    for (Object detail : child) {
                        mappingField.setAccessible(true);
                        mappingField.set(detail, entity.getUuid());
                        this.save((BaseEntity) detail);
                    }
                }
            }
        }
    }

    private Collection getChildren(Field field, Long uuid) throws Exception {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        Class clazz = oneToMany.clazz();
        String columnName = oneToMany.name();
        Entity entityAnnotation = (Entity) clazz.getAnnotation(Entity.class);
        Field[] fields = reflectUtils.getMappingField(clazz);
        String sql = "SELECT "
                + reflectUtils.getColumnNames(fields)
                + " FROM "
                + entityAnnotation.table()
                + " WHERE "
                + columnName
                + " = ?";
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql);
        preparedStatement.setLong(1, uuid);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<BaseEntity> collection = new ArrayList<>();
        while (resultSet.next()) {
            BaseEntity entity = (BaseEntity) clazz.newInstance();
            for (Field childField : fields) {
                childField.setAccessible(true);
                if (childField.getAnnotation(OneToMany.class) != null) {
                    childField.set(entity, this.getChildren(childField, entity.getUuid()));
                } else {
                    childField.set(entity, fieldTypeStrategy.getColumnValue(childField, resultSet));
                }
            }
            collection.add(entity);
        }
        resultSet.close();
        preparedStatement.close();
        return collection;
    }
}
