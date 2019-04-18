package org.hunter.pocket.criteria;

import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.connect.ConnectionManager;
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

/**
 * @author wujianchuan 2019/1/10
 */
public class CriteriaImpl extends AbstractCriteria implements Criteria {

    private final SqlFactory sqlFactory = SqlFactory.getInstance();

    public CriteriaImpl(Class clazz, Connection connection, DatabaseNodeConfig databaseConfig) {
        super(clazz, connection, databaseConfig);
    }

    @Override
    public Criteria add(Restrictions restrictions) {
        this.restrictionsList.add(restrictions);
        restrictions.pushTo(this.sortedRestrictionsList);
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
        completeSql.append(sqlFactory.getSqlBody(tableName, fields, restrictionsList, modernList, null)
                .buildUpdateSql(fieldMapper, parameters, parameterMap, databaseConfig));
        PreparedStatement preparedStatement;
        try {
            preparedStatement = getPreparedStatement();
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            this.clear();
        }
    }

    @Override
    public List list() {
        completeSql.append(sqlFactory
                .getSqlBody(tableName, fields, restrictionsList, null, orderList)
                .buildSelectSql(fieldMapper, databaseConfig)
        );
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
            this.clear();
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
        completeSql.append(sqlFactory
                .getSqlBody(tableName, fields, restrictionsList, null, orderList)
                .buildSelectSql(fieldMapper, databaseConfig)
        );
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
            this.clear();
        }
        return entity;
    }

    @Override
    public Object unique(boolean cascade) {
        PocketEntity obj = (PocketEntity) this.unique();
        if (obj != null && cascade) {
            this.applyChildren(obj);
        }
        this.clear();
        return obj;
    }

    @Override
    public long count() {
        completeSql.append(sqlFactory.getSqlBody(tableName, fields, restrictionsList, null, null)
                .buildCountSql(fieldMapper, databaseConfig)
        );
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = getPreparedStatement();
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return (long) resultSet.getObject(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
            this.clear();
        }
    }

    @Override
    public long delete() {
        completeSql.append(sqlFactory.getSqlBody(tableName, fields, restrictionsList, null, null)
                .buildDeleteSql(fieldMapper, databaseConfig)
        );
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getPreparedStatement();
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, null);
            this.clear();
        }
    }

    @Override
    public Object max(String fieldName) {
        completeSql.append(sqlFactory.getSqlBody(tableName, fields, restrictionsList, null, null)
                .buildMaxSql(fieldMapper, databaseConfig, fieldName)
        );
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = getPreparedStatement();
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getObject(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
            this.clear();
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
                        childField.set(entity, this.getChildren(childField, entity));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CriteriaException(e.getMessage());
                    }
                }
            }
        }
    }

    private Collection getChildren(Field field, PocketEntity entity) {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        Class clazz = oneToMany.clazz();
        String downBridgeFieldName = oneToMany.bridgeField();
        try {
            Field childBridgeField = clazz.getDeclaredField(downBridgeFieldName);
            ManyToOne manyToOne = childBridgeField.getAnnotation(ManyToOne.class);
            String upFieldName = manyToOne.upBridgeField();
            Field upField;
            try {
                upField = entity.getClass().getSuperclass().getDeclaredField(upFieldName);
            } catch (NoSuchFieldException e) {
                upField = entity.getClass().getDeclaredField(upFieldName);
            }
            upField.setAccessible(true);
            Object upFieldValue = upField.get(entity);
            return new CriteriaImpl(clazz, connection, databaseConfig)
                    .add(Restrictions.equ(downBridgeFieldName, upFieldValue))
                    .list();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CriteriaException(e.getMessage());
        }
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
                this.sortedRestrictionsList);
        return preparedStatement;
    }
}
