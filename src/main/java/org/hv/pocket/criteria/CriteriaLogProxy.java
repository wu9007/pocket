package org.hv.pocket.criteria;

import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.flib.ResultSetHandler;
import org.hv.pocket.logger.PersistenceLogSubject;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wujianchuan
 */
public class CriteriaLogProxy {
    private final Logger logger = LoggerFactory.getLogger(CriteriaLogProxy.class);
    private final CriteriaImpl target;
    private final Session session;
    private final Class<? extends AbstractEntity> clazz;

    public static CriteriaLogProxy newInstance(CriteriaImpl target) {
        return new CriteriaLogProxy(target);
    }

    public CriteriaLogProxy(CriteriaImpl target) {
        this.target = target;
        this.session = target.getSession();
        this.clazz = target.getClazz();
    }

    <E extends AbstractEntity> List<E> executeQuery() throws SQLException, IllegalAccessException, InstantiationException {
        PreparedStatement preparedStatement = target.getPreparedStatement();
        String sql = this.pickSql(preparedStatement);
        long startTime = System.currentTimeMillis();
        ResultSet resultSet = null;
        try {
            // 执行语句
            resultSet = preparedStatement.executeQuery();
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

            //控制台打印sql语句及执行耗时
            if (target.databaseConfig.getShowSql()) {
                long endTime = System.currentTimeMillis();
                logger.info("【SQL】 {} \n 【Milliseconds】: {}", sql, endTime - startTime);
            }

            // 生成后镜像
            if (this.target.databaseConfig.getCollectLog()) {
                PersistenceLogSubject.getInstance().pushLog(sql, null, null);
            }
        }
    }

    int executeUpdate() throws SQLException {
        // 生成前镜像
        List<?> beforeMirror = null;
        if (this.target.databaseConfig.getCollectLog()) {
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
            if (target.databaseConfig.getShowSql()) {
                long endTime = System.currentTimeMillis();
                logger.info("【SQL】 {} \n 【Milliseconds】: {}", sql, endTime - startTime);
            }

            // 生成后镜像
            if (this.target.databaseConfig.getCollectLog() && result > 0) {
                List<?> afterMirror = this.loadMirror();
                PersistenceLogSubject.getInstance().pushLog(sql, beforeMirror, afterMirror);
            }
        }
    }

    private List<?> loadMirror() {
        Criteria selectCriteria = session.createCriteria(clazz);
        target.getRestrictionsList().forEach(selectCriteria::add);
        return selectCriteria.list();
    }

    private String pickSql(PreparedStatement preparedStatement) {
        String tempSql = preparedStatement.toString();
        return tempSql.substring(tempSql.indexOf(":") + 2);
    }
}
