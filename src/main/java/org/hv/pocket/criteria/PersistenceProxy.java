package org.hv.pocket.criteria;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.flib.ResultSetHandler;
import org.hv.pocket.function.PocketFunction;
import org.hv.pocket.logger.PersistenceLogSubject;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.Session;
import org.hv.pocket.utils.EnumPocketThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * SQL语句执行代理
 * 记录执行的语句、效率、前后镜像
 *
 * @author wujianchuan
 */
public class PersistenceProxy {
    private final Logger logger = LoggerFactory.getLogger(PersistenceProxy.class);
    private final DatabaseNodeConfig databaseNodeConfig;
    private boolean showSqlLog;
    private final ExecutorService executorService;
    private Session session;
    private CriteriaImpl target;
    private Class<? extends AbstractEntity> clazz;

    private PersistenceProxy(CriteriaImpl target) {
        this.target = target;
        this.databaseNodeConfig = target.databaseConfig;
        this.showSqlLog = target.showSqlLog;
        this.session = target.getSession();
        this.clazz = target.getClazz();
        this.executorService = EnumPocketThreadPool.INSTANCE.getPersistenceLogExecutorService();
    }

    private PersistenceProxy(DatabaseNodeConfig databaseNodeConfig) {
        this.databaseNodeConfig = databaseNodeConfig;
        this.executorService = EnumPocketThreadPool.INSTANCE.getPersistenceLogExecutorService();
    }

    public static PersistenceProxy newInstance(CriteriaImpl target) {
        return new PersistenceProxy(target);
    }

    public static PersistenceProxy newInstance(DatabaseNodeConfig databaseNodeConfig) {
        return new PersistenceProxy(databaseNodeConfig);
    }

    /**
     * 代理SessionImpl中的保存操作和SQLQuery中的查询操作
     *
     * @param preparedStatement prepared statement
     * @param function          function
     * @param <R>               result
     * @return result
     * @throws SQLException e
     */
    public <R> R executeWithLog(PreparedStatement preparedStatement, PocketFunction<PreparedStatement, R> function) throws SQLException {
        String sql = this.pickSql(preparedStatement);
        long startTime = System.currentTimeMillis();
        R result;
        try {
            result = function.apply(preparedStatement);
        } finally {
            //控制台打印sql语句及执行耗时
            long milliseconds = System.currentTimeMillis() - startTime;
            this.consoleLog(sql, milliseconds);
            // 生成后镜像
            if (this.databaseNodeConfig.getCollectLog() && this.showSqlLog) {
                this.pushLog(sql, null, null, milliseconds);
            }
        }
        return result;
    }

    // =========================================== Package Private =========================================== //

    ResultSet getResultSet(PreparedStatement preparedStatement) throws SQLException {
        String sql = this.pickSql(preparedStatement);
        long startTime = System.currentTimeMillis();
        try {
            // 执行语句
            return preparedStatement.executeQuery();
        } finally {
            //控制台打印sql语句及执行耗时
            long milliseconds = System.currentTimeMillis() - startTime;
            this.consoleLog(sql, milliseconds);
            // 生成后镜像
            if (this.databaseNodeConfig.getCollectLog() && this.showSqlLog) {
                this.pushLog(sql, null, null, milliseconds);
            }
        }
    }

    <E extends AbstractEntity> List<E> executeQuery() throws SQLException, IllegalAccessException, InstantiationException {
        PreparedStatement preparedStatement = target.getPreparedStatement();
        ResultSet resultSet = null;
        try {
            // 执行语句
            resultSet = this.getResultSet(preparedStatement);
            ResultSetHandler resultSetHandler = ResultSetHandler.newInstance(resultSet);
            List<E> result = new ArrayList<>();
            while (resultSet.next()) {
                E entity = (E) clazz.newInstance();
                for (Field field : MapperFactory.getViewFields(clazz.getName())) {
                    field.set(entity, resultSetHandler.getMappingColumnValue(clazz, field));
                }
                result.add(entity);
            }
            return result;
        } finally {
            // 关闭资源
            ConnectionManager.closeIo(preparedStatement, resultSet);
        }
    }

    int executeUpdate() throws SQLException {
        // 生成前镜像
        List<? extends AbstractEntity> beforeMirror = new ArrayList<>();
        if (this.databaseNodeConfig.getCollectLog()) {
            beforeMirror = this.loadBeforeMirror();
        }
        PreparedStatement preparedStatement = target.getPreparedStatement();
        String sql = this.pickSql(preparedStatement);
        long startTime = System.currentTimeMillis();
        int result = 0;
        try {
            // 执行语句
            result = preparedStatement.executeUpdate();
            return result;
        } finally {
            // 关闭资源
            ConnectionManager.closeIo(preparedStatement, null);
            //控制台打印sql语句及执行耗时
            long milliseconds = System.currentTimeMillis() - startTime;
            this.consoleLog(sql, milliseconds);
            // 生成后镜像
            if (this.databaseNodeConfig.getCollectLog() && this.showSqlLog && result > 0) {
                this.pushLog(sql, beforeMirror, this.loadAfterMirror(beforeMirror), milliseconds);
            }
        }
    }

    // =========================================== Class Private =========================================== //

    private <E extends AbstractEntity> List<E> loadBeforeMirror() {
        Criteria selectCriteria = session.createCriteria(clazz);
        selectCriteria.withLog(false);
        target.getRestrictionsList().forEach(selectCriteria::add);
        return selectCriteria.list();
    }

    private <E extends AbstractEntity> List<E> loadAfterMirror(List<E> beforeMirror) {
        if (beforeMirror.size() > 0) {

            String identifyFieldName = MapperFactory.getIdentifyFieldName(clazz.getName());
            Criteria selectCriteria = session.createCriteria(clazz);
            selectCriteria.withLog(false);
            List<String> ids = beforeMirror.stream().map(item -> (String) item.loadIdentify()).collect(Collectors.toList());
            selectCriteria.add(Restrictions.in(identifyFieldName, ids));
            return selectCriteria.list();
        } else {
            return new ArrayList<>();
        }
    }

    private String pickSql(PreparedStatement preparedStatement) {
        String tempSql = preparedStatement.toString();
        return tempSql.substring(tempSql.indexOf(":") + 2);
    }

    private void consoleLog(String sql, long milliseconds) {
        if (this.showSqlLog) {
            this.executorService.execute(() -> logger.info("【SQL】 {} \n 【Milliseconds】: {}", sql, milliseconds));
        }
    }

    private void pushLog(String sql, List<?> beforeMirror, List<?> afterMirror, long milliseconds) {
        PersistenceLogSubject.getInstance().pushLog(sql, beforeMirror, afterMirror, milliseconds);
    }
}
