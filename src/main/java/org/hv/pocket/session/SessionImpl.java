package org.hv.pocket.session;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.ManyToOne;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.criteria.CriteriaImpl;
import org.hv.pocket.criteria.Modern;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.exception.CriteriaException;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.DetailInductiveBox;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.query.ProcessQuery;
import org.hv.pocket.query.ProcessQueryImpl;
import org.hv.pocket.query.SQLQuery;
import org.hv.pocket.query.SQLQueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
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
        if (this.getClosed()) {
            synchronized (OPEN_LOCK) {
                if (this.getClosed()) {
                    this.connection = ConnectionManager.getInstance().getConnection(databaseNodeConfig);
                    this.setClosed(false);
                    this.logger.debug("Session 【{}】 turned on", this.sessionName);
                } else {
                    this.logger.warn("This session is connected. Please don't try again");
                }
            }
        } else {
            this.logger.warn("This session is connected. Please don't try again");
        }
    }

    @Override
    public void close() {
        if (!this.getClosed()) {
            synchronized (CLOSE_LOCK) {
                if (!this.getClosed()) {
                    ConnectionManager.getInstance().closeConnection(this.databaseNodeConfig.getNodeName(), this.connection);
                    if (transaction != null) {
                        this.transaction = null;
                    }
                    this.setClosed(true);
                    this.logger.debug("Session 【{}】 turned off", this.sessionName);
                } else {
                    this.logger.warn("This session is closed. Please don't try again");
                }
            }
        } else {
            this.logger.warn("This session is closed. Please don't try again");
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
    public SQLQuery createSQLQuery() {
        return new SQLQueryImpl(this.connection, this.databaseNodeConfig);
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
    public <T extends AbstractEntity> ProcessQuery<T> createProcessQuery(String processSql) {
        return new ProcessQueryImpl<>(processSql, this.connection, this.databaseNodeConfig);
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
    public <T extends AbstractEntity> T findOne(Class<T> clazz, Serializable identify, boolean cascade) throws SQLException {
        return this.findDirect(clazz, identify, cascade);
    }

    @Override
    public <E extends AbstractEntity> List<E> list(Class<E> clazz) {
        return this.list(clazz, true);
    }

    @Override
    public <E extends AbstractEntity> List<E> list(Class<E> clazz, boolean cascade) {
        Criteria criteria = this.createCriteria(clazz);
        return criteria.list(cascade);
    }

    @Override
    public <T extends AbstractEntity> T findDirect(Class<T> clazz, Serializable identify) throws SQLException {
        return this.findDirect(clazz, identify, true);
    }

    @Override
    public <T extends AbstractEntity> T findDirect(Class<T> clazz, Serializable identify, boolean cascade) throws SQLException {
        Criteria criteria = this.createCriteria(clazz);
        criteria.add(Restrictions.equ(MapperFactory.getIdentifyFieldName(clazz.getName()), identify));
        return criteria.unique(cascade);
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
        AbstractEntity older = this.findOne(clazz, entity.loadIdentify(), false);
        int effectRow = 0;
        if (older != null) {
            Field[] fields = reflectUtils.dirtyFieldFilter(entity, older);
            boolean needUpdate = fields.length > 0 && Arrays.stream(fields).anyMatch(field -> {
                Column column = field.getAnnotation(Column.class);
                if (column != null && !column.ignoreCompare()) {
                    return true;
                }
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                return manyToOne != null && !manyToOne.ignoreCompare();
            });
            if (needUpdate) {
                Criteria criteria = this.createCriteria(clazz);
                String identifyFieldName = MapperFactory.getIdentifyFieldName(clazz.getName());
                criteria.add(Restrictions.equ(identifyFieldName, entity.loadIdentify()));
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        criteria.add(Modern.set(field.getName(), field.get(entity)));
                    } catch (IllegalAccessException e) {
                        throw new CriteriaException(e.getMessage());
                    }
                }
                effectRow += criteria.update();
            }
        }
        return effectRow;
    }

    @Override
    public int update(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = 0;
        Class<? extends AbstractEntity> clazz = entity.getClass();
        Object older = this.findOne(clazz, entity.loadIdentify(), cascade);
        if (older == null) {
            logger.warn("The old data could not be found while performing the update operation.");
        }
        if (cascade) {
            String mainClassName = entity.getClass().getName();
            Field[] fields = MapperFactory.getOneToMayFields(mainClassName);
            if (fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    DetailInductiveBox detailBox = DetailInductiveBox.newInstance((List<? extends AbstractEntity>) field.get(entity), (List<? extends AbstractEntity>) field.get(older));
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
        return this.delete(entity, true);
    }

    @Override
    public int delete(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException {
        int effectRow = 0;
        Class<? extends AbstractEntity> clazz = entity.getClass();
        if (cascade) {
            String mainClassName = clazz.getName();
            Field[] fields = MapperFactory.getOneToMayFields(mainClassName);
            if (fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    List<? extends AbstractEntity> details = (List<? extends AbstractEntity>) field.get(entity);
                    if (details != null) {
                        for (AbstractEntity detail : details) {
                            effectRow += this.delete(detail, true);
                        }
                    }
                }
            }
        }
        effectRow += this.deleteEntity(entity, cascade);
        return effectRow;
    }

    @Override
    public CacheHolder getCacheHolder() {
        // TODO
        return null;
    }
}
