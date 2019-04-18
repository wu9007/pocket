package org.hunter.pocket.criteria;

import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.constant.SqlOperateTypes;
import org.hunter.pocket.annotation.OneToMany;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.exception.CriteriaException;
import org.hunter.pocket.model.PocketEntity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hunter.pocket.exception.ErrorMessage.POCKET_ILLEGAL_FIELD_EXCEPTION;

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
            this.sqlRestriction.append(" WHERE ");
        } else {
            this.sqlRestriction.append(" AND ");
        }
        this.sqlRestriction.append(this.parseSql(restrictions));
        return this;
    }

    @Override
    public Criteria add(Modern modern) {
        this.modernList.add(modern);
        return this;
    }

    @Override
    public Criteria add(Sort order) {
        this.orderList.add(order);
        return this;
    }

    @Override
    public Criteria setParameter(String key, Object value) {
        this.parameterMap.put(key, value);
        return this;
    }

    @Override
    public Criteria limit(int start, int limit) {
        this.setLimit(start, limit);
        return this;
    }

    @Override
    public int update() {
        completeSql.append("UPDATE ")
                .append(this.tableName)
                .append(" SET ")
                .append(this.modernList.stream()
                        .map(modern -> modern.parse(fieldMapper, parameters, parameterMap))
                        .collect(Collectors.joining(", ")))
                .append(this.sqlRestriction);
        PreparedStatement preparedStatement;
        try {
            preparedStatement = getPreparedStatement();
            this.after();
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        }
    }

    @Override
    public List list() {
        completeSql.append("SELECT ")
                .append(this.reflectUtils.getColumnNames(this.fields))
                .append(" FROM ")
                .append(this.tableName)
                .append(this.sqlRestriction);
        if (this.orderList.size() > 0) {
            completeSql.append(" ORDER BY ")
                    .append(this.orderList.stream().map(order -> fieldMapper.get(order.getSource()).getColumnName() + " " + order.getSortType()).collect(Collectors.joining(",")));
        }
        if (this.limited()) {
            completeSql.append(" LIMIT ")
                    .append(this.getStart())
                    .append(", ")
                    .append(this.getLimit());
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = getPreparedStatement();
            resultSet = preparedStatement.executeQuery();
            List<PocketEntity> result = new ArrayList<>();

            while (resultSet.next()) {
                PocketEntity entity = (PocketEntity) clazz.newInstance();
                for (Field field : this.fields) {
                    field.set(entity, this.fieldTypeStrategy.getMappingColumnValue(field, resultSet));
                }
                result.add(entity);
            }
            return result;
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
            this.after();
        }
    }

    @Override
    public List list(boolean cascade) {
        List result = this.list();
        if (cascade) {
            if (result.size() > 0) {
                for (Object entity : result) {
                    this.applyChildren((PocketEntity) entity);
                }
            }
        }
        return result;
    }

    @Override
    public Object unique() {
        completeSql.append("SELECT ")
                .append(this.reflectUtils.getColumnNames(this.fields))
                .append(" FROM ")
                .append(this.tableName)
                .append(this.sqlRestriction);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        PocketEntity entity;
        try {
            preparedStatement = getPreparedStatement();
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                entity = (PocketEntity) clazz.newInstance();
                for (Field field : this.fields) {
                    field.set(entity, this.fieldTypeStrategy.getMappingColumnValue(field, resultSet));
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
            this.after();
        }
        return entity;
    }

    @Override
    public Object unique(boolean cascade) {
        PocketEntity obj = (PocketEntity) this.unique();
        if (obj != null && cascade) {
            this.applyChildren(obj);
        }
        this.after();
        return obj;
    }

    @Override
    public long count() {
        completeSql.append("SELECT ")
                .append(SqlOperateTypes.COUNT)
                .append("(0)")
                .append(" FROM ")
                .append(this.tableName)
                .append(this.sqlRestriction);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = getPreparedStatement();
            resultSet = preparedStatement.executeQuery();
            this.after();
            if (resultSet.next()) {
                return (long) resultSet.getObject(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
        }
    }

    @Override
    public long delete() {
        completeSql.append("DELETE FROM ")
                .append(this.tableName)
                .append(this.sqlRestriction);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getPreparedStatement();
            this.after();
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, null);
        }
    }

    @Override
    public Object max(String fieldName) {
        completeSql.append("SELECT ")
                .append(SqlOperateTypes.MAX)
                .append("(")
                .append(fieldMapper.get(fieldName).getColumnName())
                .append(") ")
                .append(" FROM ")
                .append(this.tableName)
                .append(this.sqlRestriction);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = getPreparedStatement();
            resultSet = preparedStatement.executeQuery();
            this.after();
            if (resultSet.next()) {
                return resultSet.getObject(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
        }
    }

    /**
     * 给实体追加子表信息
     *
     * @param entity 实体
     */
    private void applyChildren(PocketEntity entity) {
        Serializable uuid = reflectUtils.getUuidValue(entity);

        if (uuid != null && childrenFields.length > 0) {
            for (Field childField : childrenFields) {
                childField.setAccessible(true);
                if (childField.getAnnotation(OneToMany.class) != null) {
                    try {
                        childField.set(entity, this.getChildren(childField, uuid));
                    } catch (Exception e) {
                        throw new CriteriaException(e.getMessage());
                    }
                }
            }
        }
    }

    private Collection getChildren(Field field, Serializable uuid) {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        Class clazz = oneToMany.clazz();
        String columnName = oneToMany.name();
        Entity entityAnnotation = (Entity) clazz.getAnnotation(Entity.class);
        Field[] fields = reflectUtils.getMappingFields(clazz);
        String sql = "SELECT "
                + reflectUtils.getColumnNames(fields)
                + " FROM "
                + entityAnnotation.table()
                + " WHERE "
                + columnName
                + " = ?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = this.connection.prepareStatement(sql);

            preparedStatement.setObject(1, uuid);
            resultSet = preparedStatement.executeQuery();
            List<PocketEntity> collection = new ArrayList<>();

            while (resultSet.next()) {
                PocketEntity entity = (PocketEntity) clazz.newInstance();
                for (Field childField : fields) {
                    if (childField.getAnnotation(OneToMany.class) != null) {
                        Serializable childUuid = reflectUtils.getUuidValue(entity);
                        childField.setAccessible(true);
                        childField.set(entity, this.getChildren(childField, childUuid));
                    } else {
                        childField.set(entity, fieldTypeStrategy.getMappingColumnValue(childField, resultSet));
                    }
                }
                collection.add(entity);
            }
            return collection.size() > 0 ? collection : null;
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
        }
    }

    /**
     * 解析出SQL
     *
     * @param restrictions 约束对象
     * @return SQL
     */
    private String parseSql(Restrictions restrictions) {
        StringBuilder sql = new StringBuilder();
        try {

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
        } catch (NullPointerException e) {
            throw new CriteriaException(String.format(POCKET_ILLEGAL_FIELD_EXCEPTION, restrictions.getSource()));
        }
        return sql.toString();
    }

    private PreparedStatement getPreparedStatement() {
        this.before();
        PreparedStatement preparedStatement;
        try {
            preparedStatement = this.connection.prepareStatement(completeSql.toString());
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        }
        fieldTypeStrategy.setPreparedStatement(preparedStatement, this.parameters,
                this.restrictionsList);
        return preparedStatement;
    }
}
