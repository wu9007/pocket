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

/**
 * SQL语句执行代理
 * 记录执行的语句、效率、前后镜像
 *
 * @author wujianchuan
 */
public class PersistenceProxy {
    private final Logger logger = LoggerFactory.getLogger(PersistenceProxy.class);
    private final DatabaseNodeConfig databaseNodeConfig;
    private final ExecutorService executorService;
    private Session session;
    private CriteriaImpl target;
    private Class<? extends AbstractEntity> clazz;

    private PersistenceProxy(CriteriaImpl target) {
        this.target = target;
        this.databaseNodeConfig = target.databaseConfig;
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
            this.consoleLog(sql, startTime);
            // 生成后镜像
            if (this.databaseNodeConfig.getCollectLog()) {
                this.pushLog(sql, null, null);
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
            this.consoleLog(sql, startTime);
            // 生成后镜像
            if (this.databaseNodeConfig.getCollectLog()) {
                this.pushLog(sql, null, null);
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
        List<?> beforeMirror = null;
        if (this.databaseNodeConfig.getCollectLog()) {
            beforeMirror = this.loadMirror();
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
            this.consoleLog(sql, startTime);
            // 生成后镜像
            if (this.databaseNodeConfig.getCollectLog() && result > 0) {
                this.pushLog(sql, beforeMirror, this.loadMirror());
            }
        }
    }

    // =========================================== Class Private =========================================== //

    private List<?> loadMirror() {
        Criteria selectCriteria = session.createCriteria(clazz);
        target.getRestrictionsList().forEach(selectCriteria::add);
        return selectCriteria.list();
    }

    private String pickSql(PreparedStatement preparedStatement) {
        String tempSql = preparedStatement.toString();
        return tempSql.substring(tempSql.indexOf(":") + 2);
    }

    private void consoleLog(String sql, long startTime) {
        if (databaseNodeConfig.getShowSql()) {
            this.executorService.execute(() -> {
                long endTime = System.currentTimeMillis();
                logger.info("【SQL】 {} \n 【Milliseconds】: {}", sql, endTime - startTime);
            });
        }
    }

    private void pushLog(String sql, List<?> beforeMirror, List<?> afterMirror) {
        this.executorService.execute(() -> PersistenceLogSubject.getInstance().pushLog(sql, beforeMirror, afterMirror));
    }
}
