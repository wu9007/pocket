package org.hv.pocket.criteria;

import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.constant.SqlOperateTypes;
import org.hv.pocket.exception.CriteriaException;
import org.hv.pocket.flib.PreparedStatementHandler;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public class CriteriaImpl extends AbstractCriteria implements Criteria {
    private final Logger logger = LoggerFactory.getLogger(CriteriaImpl.class);
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
        this.restrictionsList.add(encryptTarget(restrictions));
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
            this.modernList.add(encryptTarget(modern));
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
    public Criteria specifyField(String... fieldNames) {
        super.setSpecifyFieldNames(fieldNames);
        return this;
    }

    @Override
    public int update() throws SQLException {
        try {
            completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildUpdateSql(parameters, parameterMap, databaseConfig));
            return PersistenceProxy.newInstance(this).executeUpdate(completeSql.toString());
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
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildSelectSql(databaseConfig, super.specifyFieldNames));
        if (this.limited()) {
            String sql = SqlFactory.getInstance().applySql(databaseConfig.getDriverName(), SqlOperateTypes.LIMIT, completeSql.toString(), new Integer[]{this.getStart(), this.getLimit()});
            completeSql = new StringBuilder(sql);
        }
        try {
            List<E> result = PersistenceProxy.newInstance(this).executeQuery(completeSql.toString(), super.specifyFieldNames);
            if (!result.isEmpty()) {
                Field[] oneToOneFields = MapperFactory.getOneToOneFields(this.clazz.getName());
                if (oneToOneFields.length > 0) {
                    for (AbstractEntity entity : result) {
                        applyOneToOneValue(entity, oneToOneFields);
                    }
                }
            }
            return result;
        } catch (IllegalAccessException | InstantiationException | SQLException e) {
            throw new CriteriaException(e.getMessage());
        } finally {
            this.cleanWithoutRestrictions();
        }
    }

    @Override
    public <E extends AbstractEntity> List<E> list(boolean cascade) {
        try {
            return this.listNotCleanRestrictions(cascade);
        } finally {
            this.cleanRestrictions();
        }
    }

    @Override
    public <E extends AbstractEntity> List<E> listNotCleanRestrictions(boolean cascade) {
        List<E> result = this.listNotCleanRestrictions();
        if (result.size() > 0 && cascade) {
            Field[] oneToMayFields = MapperFactory.getOneToMayFields(this.clazz.getName());
            if (oneToMayFields.length > 0) {
                for (AbstractEntity entity : result) {
                    this.applyOneToMany(entity, oneToMayFields);
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
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildSelectSql(databaseConfig, super.specifyFieldNames));
        if (this.limited()) {
            String sql = SqlFactory.getInstance().applySql(databaseConfig.getDriverName(), SqlOperateTypes.LIMIT, completeSql.toString(), new Integer[]{this.getStart(), this.getLimit()});
            completeSql = new StringBuilder(sql);
        }
        try {
            T result;
            List<T> resultList = PersistenceProxy.newInstance(this).executeQuery(completeSql.toString(), super.specifyFieldNames);
            if (resultList.size() > 1) {
                throw new CriteriaException("Data is not unique, and multiple data are returned.");
            } else if (resultList.size() == 0) {
                result = null;
            } else {
                result = resultList.get(0);
                Field[] oneToOneFields = MapperFactory.getOneToOneFields(this.clazz.getName());
                if (oneToOneFields.length > 0) {
                    applyOneToOneValue(result, oneToOneFields);
                }
            }
            return result;
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
            Field[] oneToMayFields = MapperFactory.getOneToMayFields(this.clazz.getName());
            if (oneToMayFields.length > 0) {
                this.applyOneToMany(entity, oneToMayFields);
            }
        }
        this.cleanAll();
        return entity;
    }

    @Override
    public Number count() throws SQLException {
        completeSql.append(SqlBody.newInstance(clazz, restrictionsList, modernList, orderList).buildCountSql(databaseConfig));
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = getPreparedStatement();
            resultSet = PersistenceProxy.newInstance(this).getResultSet(preparedStatement, completeSql.toString());
            if (resultSet.next()) {
                return (Number) resultSet.getObject(1);
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
            return PersistenceProxy.newInstance(this).executeUpdate(completeSql.toString());
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
            resultSet = PersistenceProxy.newInstance(this).getResultSet(preparedStatement, completeSql.toString());
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

    // ================================================= private ==================================================== //

    /**
     * 给实例追加一对一关联数据
     *
     * @param result         实例
     * @param oneToOneFields 关联属性数组
     * @param <T>            持久化类行
     */
    private <T extends AbstractEntity> void applyOneToOneValue(T result, Field[] oneToOneFields) {
        for (Field oneToOneField : oneToOneFields) {
            try {
                Class<? extends AbstractEntity> oneToOneClass = MapperFactory.getOneToOneClass(this.clazz.getName(), oneToOneField.getName());
                String relationFieldName = MapperFactory.getOneToOneRelationFieldName(this.clazz.getName(), oneToOneField.getName());
                String ownFieldName = MapperFactory.getOneToOneOwnFieldName(this.clazz.getName(), oneToOneField.getName());
                Field ownField = MapperFactory.getField(this.clazz.getName(), ownFieldName);
                ownField.setAccessible(true);
                Object relationValue = ownField.get(result);
                if (relationValue != null) {
                    Criteria criteria = new CriteriaImpl(oneToOneClass, session)
                            .add(Restrictions.equ(relationFieldName, relationValue));
                    Object value = criteria.unique();
                    oneToOneField.setAccessible(true);
                    oneToOneField.set(result, value);
                }
            } catch (Exception e) {
                throw new CriteriaException(e.getMessage());
            }
        }
    }

    /**
     * 给实例追加一对多关联数据
     *
     * @param entity 实例
     * @param fields 关联属性数组
     * @param <T>    持久化类行
     */
    private <T extends AbstractEntity> void applyOneToMany(T entity, Field[] fields) {
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
                        this.applyOneToMany(detail, detailFields);
                    }
                }
            } catch (Exception e) {
                throw new CriteriaException(e.getMessage());
            }
        }
    }

    PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement;
        preparedStatement = this.connection.prepareStatement(completeSql.toString());
        logger.debug("Creates a <code>PreparedStatement</code> object");
        PreparedStatementHandler preparedStatementHandler = PreparedStatementHandler.newInstance(this.clazz, preparedStatement);
        preparedStatementHandler.completionPreparedStatement(this.parameters, this.sortedRestrictionsList);
        return preparedStatement;
    }
}
