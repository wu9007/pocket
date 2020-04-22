package org.hv.pocket.criteria;

import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.exception.CriteriaException;
import org.hv.pocket.flib.PreparedStatementHandler;
import org.hv.pocket.flib.ResultSetHandler;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.session.Session;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public class CriteriaImpl extends AbstractCriteria implements Criteria {

    public CriteriaImpl(Class clazz, Session session) {
        super(clazz, session);
    }

    @Override
    public Criteria add(Restrictions restrictions) {
        String source = restrictions.getSource();
        boolean sourceEmpty = source == null || source.trim().length() == 0;
        if (sourceEmpty && restrictions.getSqlOperate() == null) {
            throw new CriteriaException("No field name found in your Restrictions.");
        }
        this.restrictionsList.add(restrictions);
        restrictions.pushTo(this.sortedRestrictionsList);
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
            return super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeUpdate);
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
            resultSet = super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeQuery);
            ResultSetHandler resultSetHandler = ResultSetHandler.newInstance(resultSet);
            List<AbstractEntity> result = new ArrayList<>();

            while (resultSet.next()) {
                AbstractEntity entity = (AbstractEntity) clazz.newInstance();
                for (Field field : MapperFactory.getViewFields(clazz.getName())) {
                    field.set(entity, resultSetHandler.getMappingColumnValue(clazz, field));
                }
                result.add(entity);
            }
            return result;
        } catch (IllegalAccessException | InstantiationException | SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            ConnectionManager.closeIo(preparedStatement, resultSet);
            this.cleanWithoutRestrictions();
        }
    }

    @Override
    public List list(boolean cascade) {
        List<AbstractEntity> result = this.list();
        if (result.size() > 0 && cascade) {
            Field[] fields = MapperFactory.getOneToMayFields(this.clazz.getName());
            if (fields.length > 0) {
                for (AbstractEntity entity : result) {
                    this.applyChildren(entity, fields);
                }
            }
        }
        return result;
    }

    @Override
    public Object top() {
        return this.top(false);
    }

    @Override
    public Object top(boolean cascade) {
        List listResult = this.list(cascade);
        return listResult != null && listResult.size() > 0 ? listResult.get(0) : null;
    }

    @Override
    public Object unique() throws SQLException {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildSelectSql(databaseConfig));
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        AbstractEntity entity = null;
        try {
            preparedStatement = getPreparedStatement();
            resultSet = super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeQuery);
            ResultSetHandler resultSetHandler = ResultSetHandler.newInstance(resultSet);
            int resultRowCount = 0;
            while (resultSet.next()) {
                if (++resultRowCount > 1) {
                    throw new CriteriaException("Data is not unique, and multiple data are returned.");
                }
                try {
                    entity = (AbstractEntity) clazz.newInstance();
                    for (Field field : MapperFactory.getViewFields(clazz.getName())) {
                        field.set(entity, resultSetHandler.getMappingColumnValue(clazz, field));
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            ConnectionManager.closeIo(preparedStatement, resultSet);
            this.cleanAll();
        }
        return entity;
    }

    @Override
    public Object unique(boolean cascade) throws SQLException {
        AbstractEntity entity = (AbstractEntity) this.unique();
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
            resultSet = super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeQuery);
            if (resultSet.next()) {
                return (long) resultSet.getObject(1);
            } else {
                return 0;
            }
        } finally {
            ConnectionManager.closeIo(preparedStatement, resultSet);
            this.cleanAll();
        }
    }

    @Override
    public int delete() throws SQLException {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildDeleteSql(databaseConfig));
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getPreparedStatement();
            return super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeUpdate);
        } finally {
            ConnectionManager.closeIo(preparedStatement, null);
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
            resultSet = super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeQuery);
            if (resultSet.next()) {
                return resultSet.getObject(1);
            } else {
                return null;
            }
        } finally {
            ConnectionManager.closeIo(preparedStatement, resultSet);
            this.cleanAll();
        }
    }

    /**
     * 给实体追加子表信息
     *
     * @param entity 实体
     */
    private void applyChildren(AbstractEntity entity, Field[] fields) {
        if (fields.length > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    String mainClassName = entity.getClass().getName();
                    String mainFieldName = field.getName();
                    Class childClass = MapperFactory.getDetailClass(mainClassName, mainFieldName);
                    String downBridgeFieldName = MapperFactory.getOneToMayDownFieldName(mainClassName, mainFieldName);
                    Object upFieldValue = MapperFactory.getUpBridgeFieldValue(entity, mainClassName, childClass);
                    Criteria criteria = new CriteriaImpl(childClass, super.getSession())
                            .add(Restrictions.equ(downBridgeFieldName, upFieldValue));
                    List<AbstractEntity> details = criteria.list();
                    field.set(entity, details);
                    Field[] detailFields = MapperFactory.getOneToMayFields(childClass.getName());
                    if (detailFields.length > 0) {
                        for (AbstractEntity detail : details) {
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
        PreparedStatement preparedStatement;
        preparedStatement = this.connection.prepareStatement(completeSql.toString());
        PreparedStatementHandler preparedStatementHandler = PreparedStatementHandler.newInstance(preparedStatement);
        preparedStatementHandler.completionPreparedStatement(this.parameters, this.sortedRestrictionsList);
        return preparedStatement;
    }
}
