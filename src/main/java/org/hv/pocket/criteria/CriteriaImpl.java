package org.hv.pocket.criteria;

import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.exception.CriteriaException;
import org.hv.pocket.flib.PreparedStatementHandler;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.session.Session;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public class CriteriaImpl extends AbstractCriteria implements Criteria {

    public CriteriaImpl(Class<? extends AbstractEntity> clazz, Session session) {
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
    public Criteria withLog(boolean withLog) {
        super.showLog(withLog);
        return this;
    }

    @Override
    public int update() throws SQLException {
        try {
            completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildUpdateSql(parameters, parameterMap, databaseConfig));
            return PersistenceProxy.newInstance(this).executeUpdate();
        } finally {
            this.cleanAll();
        }
    }

    @Override
    public <E extends AbstractEntity> List<E> list() {
        try {
            return this.listNotCleanRestrictions();
        } finally {
            this.cleanRestrictions();
        }
    }

    @Override
    public <E extends AbstractEntity> List<E> listNotCleanRestrictions() {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildSelectSql(databaseConfig));
        if (this.limited()) {
            completeSql.append(CommonSql.LIMIT)
                    .append(this.getStart())
                    .append(CommonSql.COMMA)
                    .append(this.getLimit());
        }
        try {
            return PersistenceProxy.newInstance(this).executeQuery();
        } catch (IllegalAccessException | InstantiationException | SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            this.cleanWithoutRestrictions();
        }
    }

    @Override
    public <E extends AbstractEntity> List<E> list(boolean cascade) {
        List<E> result = this.list();
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
    public <T extends AbstractEntity> T top() {
        return this.top(false);
    }

    @Override
    public <T extends AbstractEntity> T top(boolean cascade) {
        List<T> listResult = this.list(cascade);
        return listResult != null && listResult.size() > 0 ? listResult.get(0) : null;
    }

    @Override
    public <T extends AbstractEntity> T unique() {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildSelectSql(databaseConfig));
        try {
            List<T> resultList = PersistenceProxy.newInstance(this).executeQuery();
            if (resultList.size() > 1) {
                throw new CriteriaException("Data is not unique, and multiple data are returned.");
            } else if (resultList.size() == 0) {
                return null;
            } else {
                return resultList.get(0);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            this.cleanAll();
        }
    }

    @Override
    public <T extends AbstractEntity> T unique(boolean cascade) {
        T entity = this.unique();
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
            resultSet = PersistenceProxy.newInstance(this).getResultSet(preparedStatement);
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
        try {
            return PersistenceProxy.newInstance(this).executeUpdate();
        } finally {
            this.cleanAll();
        }
    }

    @Override
    public Object max(String fieldName) {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildMaxSql(databaseConfig, fieldName, true));
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = getPreparedStatement();
            resultSet = PersistenceProxy.newInstance(this).getResultSet(preparedStatement);
            if (resultSet.next()) {
                return resultSet.getObject(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new CriteriaException(e.getMessage());
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
                    Class<? extends AbstractEntity> childClass = MapperFactory.getDetailClass(mainClassName, mainFieldName);
                    String downBridgeFieldName = MapperFactory.getOneToMayDownFieldName(mainClassName, mainFieldName);
                    Object upFieldValue = MapperFactory.getUpBridgeFieldValue(entity, mainClassName, childClass);
                    Criteria criteria = new CriteriaImpl(childClass, super.getSession())
                            .add(Restrictions.equ(downBridgeFieldName, upFieldValue));
                    List<? extends AbstractEntity> details = criteria.list();
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

    PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement;
        preparedStatement = this.connection.prepareStatement(completeSql.toString());
        PreparedStatementHandler preparedStatementHandler = PreparedStatementHandler.newInstance(preparedStatement);
        preparedStatementHandler.completionPreparedStatement(this.parameters, this.sortedRestrictionsList);
        return preparedStatement;
    }
}
