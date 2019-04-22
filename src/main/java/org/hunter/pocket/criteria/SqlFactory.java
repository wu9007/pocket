package org.hunter.pocket.criteria;

import org.hunter.pocket.constant.DatasourceDriverTypes;
import org.hunter.pocket.constant.SqlOperateTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/14
 */
public class SqlFactory {
    private static final Map<String, Map<String, String>> SQL_POOL = new HashMap<>();

    static {
        Map<String, String> mysqlSqlOperateTypes = new HashMap<>(20);
        mysqlSqlOperateTypes.put(SqlOperateTypes.AND, " AND ");
        mysqlSqlOperateTypes.put(SqlOperateTypes.EQ, " = ");
        mysqlSqlOperateTypes.put(SqlOperateTypes.NE, " <> ");
        mysqlSqlOperateTypes.put(SqlOperateTypes.LIKE, " LIKE ");
        mysqlSqlOperateTypes.put(SqlOperateTypes.GT, " > ");
        mysqlSqlOperateTypes.put(SqlOperateTypes.LT, " < ");
        mysqlSqlOperateTypes.put(SqlOperateTypes.GTE, " >= ");
        mysqlSqlOperateTypes.put(SqlOperateTypes.LTE, " <= ");
        mysqlSqlOperateTypes.put(SqlOperateTypes.OR, " OR ");
        SQL_POOL.put(DatasourceDriverTypes.MYSQL_DRIVER, mysqlSqlOperateTypes);
    }

    private static final SqlFactory OUR_INSTANCE = new SqlFactory();

    public static SqlFactory getInstance() {
        return OUR_INSTANCE;
    }

    private SqlFactory() {
    }

    String getSql(String driverName, String sqlOperateType) {
        return SQL_POOL.get(driverName).get(sqlOperateType);
    }
}
