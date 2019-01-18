package org.homo.pocket.criteria;

import org.homo.pocket.model.BaseEntity;
import org.homo.pocket.annotation.Entity;
import org.homo.pocket.annotation.OneToMany;
import org.homo.pocket.config.DatabaseNodeConfig;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public class CriteriaImpl extends AbstractCriteria implements Criteria {

    public CriteriaImpl(Class clazz, Connection connection, DatabaseNodeConfig databaseConfig) {
        super(clazz, connection, databaseConfig);
    }

    @Override
    public Criteria add(Restrictions restrictions) {
        if (this.restrictionsList.size() == 0) {
            this.sql.append(" WHERE ");
        } else {
            this.sql.append(" AND ");
        }
        this.sql.append(this.parseSql(restrictions));
        return this;
    }

    @Override
    public List list() throws Exception {
        this.before();
        List<BaseEntity> result = new ArrayList<>();
        PreparedStatement preparedStatement = this.connection.prepareStatement(sql.toString());
        fieldTypeStrategy.setPreparedStatement(preparedStatement, this.restrictionsList);
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
        return result;
    }

    @Override
    public List list(boolean cascade) throws Exception {
        List result = this.list();
        if (cascade) {
            if (result.size() > 0) {
                for (Object entity : result) {
                    this.applyChildren((BaseEntity) entity);
                }
            }
        }
        return result;
    }

    @Override
    public Object unique() throws Exception {
        this.before();
        PreparedStatement preparedStatement = this.connection.prepareStatement(sql.toString());
        fieldTypeStrategy.setPreparedStatement(preparedStatement, this.restrictionsList);
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
        return entity;
    }

    @Override
    public Object unique(boolean cascade) throws Exception {
        BaseEntity obj = (BaseEntity) this.unique();
        if (cascade) {
            this.applyChildren(obj);
        }
        return obj;
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

        PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
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
        Object target = restrictions.getTarget();
        StringBuilder sql = new StringBuilder();
        if (restrictions.getLeftRestrictions() == null) {
            sql.append(fieldMapper.get(restrictions.getSource()).getColumnName())
                    .append(this.sqlFactory.getSql(this.databaseConfig.getDriverName(), restrictions.getSqlOperate()))
                    .append("?");
            this.restrictionsList.add(restrictions);
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
