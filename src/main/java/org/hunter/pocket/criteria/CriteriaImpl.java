package org.hunter.pocket.criteria;

import com.mysql.cj.jdbc.result.ResultSetImpl;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.exception.CriteriaException;
import org.hunter.pocket.model.MapperFactory;
import org.hunter.pocket.model.BaseEntity;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        String source = restrictions.getSource();
        boolean sourceEmpty = source == null || source.trim().length() == 0;
        if (sourceEmpty && restrictions.getSqlOperate() == null) {
            throw new CriteriaException("No field name found in your Restrictions.");
        }
        this.restrictionsList.add(restrictions);
        if (restrictions.getTarget() != null || restrictions.getLeftRestrictions() != null) {
            restrictions.pushTo(this.sortedRestrictionsList);
        }
        return this;
    }

    @Override
    public Criteria add(Modern modern) {
        String source = modern.getSource();
        String poEl = modern.getPoEl();
        boolean sourceValuable = source != null && source.trim().length() > 0 && MapperFactory.getRepositoryColumnName(this.clazz.getName(), source) != null;
        boolean poElValuable = poEl != null && poEl.trim().length() > 0;
        if (sourceValuable || poElValuable) {
            this.modernList.add(modern);
        } else {
            throw new CriteriaException("No field name found in your Modern.");
        }
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
    public int update() throws SQLException {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildUpdateSql(parameters, parameterMap, databaseConfig));
        PreparedStatement preparedStatement;
        try {
            preparedStatement = getPreparedStatement();
            return preparedStatement.executeUpdate();
        } finally {
            this.cleanAll();
        }
    }

    @Override
    public List list() {
        try {
            return this.listNotCleanRestrictions();
        } finally {
            this.cleanRestrictions();
        }
    }

    @Override
    public List listNotCleanRestrictions() {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildSelectSql(databaseConfig));
        if (this.limited()) {
            completeSql.append(CommonSql.LIMIT)
                    .append(this.getStart())
                    .append(CommonSql.COMMA)
                    .append(this.getLimit());
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = getPreparedStatement();
            resultSet = preparedStatement.executeQuery();
            List<BaseEntity> result = new ArrayList<>();

            while (resultSet.next()) {
                BaseEntity entity = (BaseEntity) clazz.newInstance();
                for (Field field : MapperFactory.getViewFields(clazz.getName())) {
                    field.set(entity, this.fieldTypeStrategy.getMappingColumnValue(clazz, field, resultSet));
                }
                result.add(entity);
            }
            return result;
        } catch (IllegalAccessException | InstantiationException | SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
            this.cleanWithoutRestrictions();
        }
    }

    @Override
    public List list(boolean cascade) {
        List<BaseEntity> result = this.list();
        if (result.size() > 0 && cascade) {
            Field[] fields = MapperFactory.getOneToMayFields(this.clazz.getName());
            if (fields.length > 0) {
                for (BaseEntity entity : result) {
                    this.applyChildren(entity, fields);
                }
            }
        }
        return result;
    }

    @Override
    public Object unique() throws SQLException {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildSelectSql(databaseConfig));
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        BaseEntity entity = null;
        try {
            preparedStatement = getPreparedStatement();
            resultSet = preparedStatement.executeQuery();
            int resultRowCount = ((ResultSetImpl) resultSet).getRows().size();
            if (resultRowCount > 1) {
                throw new CriteriaException("Data is not unique, and multiple data are returned.");
            }
            if (resultSet.next()) {
                try {
                    entity = (BaseEntity) clazz.newInstance();
                    for (Field field : MapperFactory.getViewFields(clazz.getName())) {
                        field.set(entity, this.fieldTypeStrategy.getMappingColumnValue(clazz, field, resultSet));
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
            this.cleanAll();
        }
        return entity;
    }

    @Override
    public Object unique(boolean cascade) throws SQLException {
        BaseEntity entity = (BaseEntity) this.unique();
        if (entity != null && cascade) {
            Field[] fields = MapperFactory.getOneToMayFields(this.clazz.getName());
            if (fields.length > 0) {
                this.applyChildren(entity, fields);
            }
        }
        this.cleanAll();
        return entity;
    }

    @Override
    public long count() throws SQLException {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildCountSql(databaseConfig));
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
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
            this.cleanAll();
        }
    }

    @Override
    public long delete() throws SQLException {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildDeleteSql(databaseConfig));
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getPreparedStatement();
            return preparedStatement.executeUpdate();
        } finally {
            ConnectionManager.closeIO(preparedStatement, null);
            this.cleanAll();
        }
    }

    @Override
    public Object max(String fieldName) throws SQLException {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildMaxSql(databaseConfig, fieldName));
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
        } finally {
            ConnectionManager.closeIO(preparedStatement, resultSet);
            this.cleanAll();
        }
    }

    /**
     * 给实体追加子表信息
     *
     * @param entity 实体
     */
    private void applyChildren(BaseEntity entity, Field[] fields) {
        if (fields.length > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    String mainClassName = entity.getClass().getName();
                    String mainFieldName = field.getName();
                    Class childClass = MapperFactory.getDetailClass(mainClassName, mainFieldName);
                    String downBridgeFieldName = MapperFactory.getOneToMayDownFieldName(mainClassName, mainFieldName);
                    Object upFieldValue = MapperFactory.getUpBridgeFieldValue(entity, mainClassName, childClass);
                    Criteria criteria = new CriteriaImpl(childClass, connection, databaseConfig)
                            .add(Restrictions.equ(downBridgeFieldName, upFieldValue));
                    List<BaseEntity> details = criteria.list();
                    field.set(entity, details);
                    Field[] detailFields = MapperFactory.getOneToMayFields(childClass.getName());
                    if (detailFields.length > 0) {
                        for (BaseEntity detail : details) {
                            this.applyChildren(detail, detailFields);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CriteriaException(e.getMessage());
                }
            }
        }
    }

    private PreparedStatement getPreparedStatement() throws SQLException {
        this.before();
        PreparedStatement preparedStatement;
        preparedStatement = this.connection.prepareStatement(completeSql.toString());
        fieldTypeStrategy.setPreparedStatement(preparedStatement, this.parameters,
                this.sortedRestrictionsList);
        return preparedStatement;
    }
}
