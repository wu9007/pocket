package org.hv.pocket.session;

import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.annotation.Entity;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.criteria.CriteriaImpl;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.model.DetailInductiveBox;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.query.ProcessQuery;
import org.hv.pocket.query.ProcessQueryImpl;
import org.hv.pocket.query.SQLQuery;
import org.hv.pocket.query.SQLQueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wujianchuan 2019/1/1
 */
public class SessionImpl extends AbstractSession {
    private final Logger logger = LoggerFactory.getLogger(SessionImpl.class);

    private static final String OPEN_LOCK = "OPEN_MONITOR";
    private static final String CLOSE_LOCK = "CLOSE_MONITOR";
    private static final String TRANSACTION_LOCK = "TRANSACTION_MONITOR";

    SessionImpl(DatabaseNodeConfig databaseNodeConfig, String sessionName) {
        super(databaseNodeConfig, sessionName);
    }

    @Override
    public void open() {
        if (this.connection == null) {
            synchronized (OPEN_LOCK) {
                if (this.connection == null) {
                    this.connection = ConnectionManager.getInstance().getConnection(databaseNodeConfig);
                    this.setClosed(false);
                    this.logger.info("Session: {} turned on.", this.sessionName);
                } else {
                    this.logger.warn("This session is connected. Please don't try again.");
                }
            }
        } else {
            this.logger.warn("This session is connected. Please don't try again.");
        }
    }

    @Override
    public void close() {
        if (this.connection != null) {
            synchronized (CLOSE_LOCK) {
                if (this.connection != null) {
                    ConnectionManager.getInstance().closeConnection(this.databaseNodeConfig.getNodeName(), this.connection);
                    this.transaction = null;
                    this.connection = null;
                    this.setClosed(true);
                    this.logger.info("Session: {} turned off.", this.sessionName);
                } else {
                    this.logger.warn("This session is closed. Please don't try again.");
                }
            }
        } else {
            this.logger.warn("This session is closed. Please don't try again.");
        }
    }

    @Override
    public Transaction getTransaction() {
        if (this.transaction == null) {
            synchronized (TRANSACTION_LOCK) {
                if (this.transaction == null) {
                    this.transaction = new TransactionImpl(this.connection);
                }
            }
        }
        return this.transaction;
    }

    @Override
    public SQLQuery createSQLQuery(String sql) {
        return new SQLQueryImpl(sql, this.connection, this.databaseNodeConfig);
    }

    @Override
    public SQLQuery createSQLQuery(String sql, Class<?> clazz) {
        return new SQLQueryImpl(sql, connection, this.databaseNodeConfig, clazz);
    }

    @Override
    public <T extends AbstractEntity> ProcessQuery<T> createProcessQuery(String processSQL) {
        return new ProcessQueryImpl<>(processSQL, this.connection, this.databaseNodeConfig);
    }

    @Override
    public Criteria createCriteria(Class<? extends AbstractEntity> clazz) {
        return new CriteriaImpl(clazz, this);
    }

    @Override
    public <T extends AbstractEntity> T findOne(Class<T> clazz, Serializable identify) throws SQLException {
        return this.findDirect(clazz, identify);
    }

    @Override
    public <E extends AbstractEntity> List<E> list(Class<E> clazz) {
        Criteria criteria = this.createCriteria(clazz);
        return criteria.list(true);
    }

    @Override
    public <T extends AbstractEntity> T findDirect(Class<T> clazz, Serializable identify) throws SQLException {
        Criteria criteria = this.createCriteria(clazz);
        criteria.add(Restrictions.equ(MapperFactory.getIdentifyFieldName(clazz.getName()), identify));
        return criteria.unique(true);
    }

    @Override
    public int save(AbstractEntity entity) throws SQLException {
        return super.saveEntity(entity, false);
    }

    @Override
    public int save(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = this.save(entity);
        if (cascade) {
            effectRow += super.saveDetails(entity, false);
        }
        return effectRow;
    }

    @Override
    public int forcibleSave(AbstractEntity entity) throws SQLException {
        return super.saveEntity(entity, true);
    }

    @Override
    public int forcibleSave(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = this.forcibleSave(entity);
        if (cascade) {
            effectRow += super.saveDetails(entity, true);
        }
        return effectRow;
    }

    @Override
    public int update(AbstractEntity entity) throws SQLException {
        Class<? extends AbstractEntity> clazz = entity.getClass();
        AbstractEntity older = this.findOne(clazz, entity.loadIdentify());
        int effectRow = 0;
        if (older != null) {
            Field[] fields = reflectUtils.dirtyFieldFilter(entity, older);
            if (fields.length > 0) {
                StringBuilder sql = new StringBuilder(CommonSql.UPDATE)
                        .append(MapperFactory.getTableName(clazz.getName()))
                        .append(CommonSql.SET);

                List<String> setValues = new LinkedList<>();
                for (Field field : fields) {
                    setValues.add(MapperFactory.getRepositoryColumnName(clazz.getName(), field.getName()) + CommonSql.EQUAL_TO + CommonSql.PLACEHOLDER);
                }
                sql.append(String.join(CommonSql.COMMA, setValues))
                        .append(CommonSql.WHERE)
                        .append(MapperFactory.getIdentifyColumnName(clazz.getName())).append(CommonSql.EQUAL_TO).append(CommonSql.PLACEHOLDER);
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = this.connection.prepareStatement(sql.toString());
                    this.statementApply(fields, entity, preparedStatement);
                    preparedStatement.setObject(fields.length + 1, entity.loadIdentify());
                    effectRow = super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeUpdate);
                } finally {
                    ConnectionManager.closeIo(preparedStatement, null);
                }
            }
        }
        return effectRow;
    }

    @Override
    public int update(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = 0;
        Class<? extends AbstractEntity> clazz = entity.getClass();
        Object older = this.findOne(clazz, entity.loadIdentify());
        if (cascade) {
            String mainClassName = entity.getClass().getName();
            Field[] fields = MapperFactory.getOneToMayFields(mainClassName);
            if (fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    DetailInductiveBox detailBox = DetailInductiveBox.newInstance((List<? extends AbstractEntity>)field.get(entity), (List<? extends AbstractEntity>)field.get(older));
                    List<? extends AbstractEntity> newbornDetails = detailBox.getNewborn();
                    if (newbornDetails.size() > 0) {
                        Class<? extends AbstractEntity> childrenClass = MapperFactory.getDetailClass(mainClassName, field.getName());
                        String downBridgeFieldName = MapperFactory.getOneToMayDownFieldName(mainClassName, field.getName());
                        Field downBridgeField = MapperFactory.getField(childrenClass.getName(), downBridgeFieldName);
                        Object upBridgeFieldValue = MapperFactory.getUpBridgeFieldValue(entity, mainClassName, childrenClass);
                        downBridgeField.setAccessible(true);
                        for (AbstractEntity detail : newbornDetails) {
                            downBridgeField.set(detail, upBridgeFieldValue);
                            this.save(detail, true);
                        }
                    }
                    for (AbstractEntity detail : detailBox.getMoribund()) {
                        this.delete(detail);
                    }
                    for (AbstractEntity detail : detailBox.getUpdate()) {
                        this.update(detail, true);
                    }
                    effectRow += detailBox.getCount();
                }
            }
        }
        effectRow += this.update(entity);
        return effectRow;
    }

    @Override
    public int delete(AbstractEntity entity) throws SQLException, IllegalAccessException {
        Class<? extends AbstractEntity> clazz = entity.getClass();
        String mainClassName = clazz.getName();
        Serializable identify = entity.loadIdentify();

        Object garbage = this.findOne(clazz, identify);
        int effectRow = 0;
        if (garbage != null) {
            // delete detail list data
            Field[] fields = MapperFactory.getOneToMayFields(mainClassName);
            if (fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    List<? extends AbstractEntity> details = (List<? extends AbstractEntity>) field.get(entity);
                    if (details != null) {
                        for (AbstractEntity detail : details) {
                            effectRow += this.delete(detail);
                        }
                    }
                }
            }

            // delete main data
            String sql = CommonSql.DELETE +
                    CommonSql.FROM +
                    MapperFactory.getTableName(clazz.getName()) +
                    CommonSql.WHERE +
                    MapperFactory.getIdentifyColumnName(clazz.getName()) +
                    CommonSql.EQUAL_TO +
                    CommonSql.PLACEHOLDER;
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = this.connection.prepareStatement(sql);
                preparedStatement.setObject(1, identify);
                effectRow += super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeUpdate);
            } finally {
                ConnectionManager.closeIo(preparedStatement, null);
            }
        }
        return effectRow;
    }

    @Override
    public long getMaxIdentify(Integer serverId, Class<? extends AbstractEntity> clazz) throws SQLException {
        Entity annotation = clazz.getAnnotation(Entity.class);
        String identifyColumnName = MapperFactory.getIdentifyColumnName(clazz.getName());
        String sql = CommonSql.SELECT
                + "MAX(CONVERT(" + identifyColumnName + " ,SIGNED))"
                + CommonSql.FROM + annotation.table()
                + CommonSql.WHERE
                + identifyColumnName
                + " REGEXP '^" + serverId + annotation.tableId() + "'";
        PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
        ResultSet resultSet = super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeQuery);
        long identify;
        if (resultSet.next()) {
            identify = resultSet.getLong(1);
        } else {
            identify = 0;
        }
        resultSet.close();
        preparedStatement.close();
        return identify;
    }

    @Override
    public CacheHolder getCacheHolder() {
        // TODO
        return null;
    }
}
