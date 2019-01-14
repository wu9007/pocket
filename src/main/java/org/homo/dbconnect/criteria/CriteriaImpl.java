package org.homo.dbconnect.criteria;

import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.annotation.Entity;
import org.homo.dbconnect.annotation.OneToMany;
import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.transaction.Transaction;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public class CriteriaImpl extends AbstractCriteria implements Criteria {

    public CriteriaImpl(Class clazz, Transaction transaction, AbstractDatabaseConfig databaseConfig) {
        super(clazz, transaction, databaseConfig);
    }

    @Override
    public void add(Restrictions restrictions) {
        if (this.restrictionsList.size() == 0) {
            this.sql.append(" WHERE ");
        }
        this.sql.append(this.parseSql(restrictions));
        this.restrictionsList.add(restrictions);
    }

    @Override
    public List<BaseEntity> list() throws Exception {
        this.before();
        List<BaseEntity> result = new ArrayList<>();
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        try {
            while (resultSet.next()) {
                BaseEntity entity = (BaseEntity) clazz.newInstance();
                for (Field field : this.fields) {
                    field.setAccessible(true);
                    field.set(entity, this.fieldTypeStrategy.getColumnValue(field, resultSet));
                }
                result.add(entity);
            }
        } finally {
            resultSet.close();
            preparedStatement.close();
        }
        if (result.size() > 0) {
            for (BaseEntity entity : result) {
                this.applyChildren(entity);
            }
        }
        return result;
    }

    @Override
    public BaseEntity unique() throws Exception {
        this.before();
        PreparedStatement preparedStatement = this.transaction.getConnection().prepareStatement(sql.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        BaseEntity entity = (BaseEntity) clazz.newInstance();
        try {
            if (resultSet.next()) {
                for (Field field : this.fields) {
                    field.setAccessible(true);
                    field.set(entity, this.fieldTypeStrategy.getColumnValue(field, resultSet));
                }
            } else {
                return null;
            }
        } finally {
            resultSet.close();
            preparedStatement.close();
        }
        applyChildren(entity);
        return entity;
    }

    /**
     * 给实体追加子表信息
     *
     * @param entity 实体
     * @throws Exception 异常
     */
    private void applyChildren(BaseEntity entity) throws Exception {
        if (entity.getUuid() != null && childrenFields.length > 0) {
            for (Field childField : childrenFields) {
                childField.setAccessible(true);
                if (childField.getAnnotation(OneToMany.class) != null) {
                    childField.set(entity, this.getChildren(childField, entity.getUuid()));
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
        try {
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
        } finally {
            resultSet.close();
            preparedStatement.close();
        }
        return collection;
    }

    /**
     * 解析出SQL
     *
     * @param restrictions 约束对象
     * @return SQL
     */
    private String parseSql(Restrictions restrictions) {
        StringBuilder sql = new StringBuilder();
        if (restrictions.getLeftRestrictions() == null) {
            sql.append(fieldMapper.get(restrictions.getSource()).getColumnName())
                    .append(this.sqlFactory.getSql(this.databaseConfig.getDriverName(), restrictions.getSqlOperate()))
                    .append(restrictions.getTarget());
        } else {
            sql.append("(")
                    .append(this.parseSql(restrictions.getLeftRestrictions()))
                    .append(this.sqlFactory.getSql(this.databaseConfig.getDriverName(), restrictions.getSqlOperate()))
                    .append(this.parseSql(restrictions.getRightRestrictions()))
                    .append(")");
        }
        return sql.toString();
    }
}
