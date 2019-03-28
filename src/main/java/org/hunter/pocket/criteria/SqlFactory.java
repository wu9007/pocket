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
        Map<String, String> mysqlSyntax = new HashMap<>(20);
        mysqlSyntax.put(SqlOperateTypes.AND, " AND ");
        mysqlSyntax.put(SqlOperateTypes.EQ, " = ");
        mysqlSyntax.put(SqlOperateTypes.NE, " <> ");
        mysqlSyntax.put(SqlOperateTypes.LIKE, " LIKE ");
        mysqlSyntax.put(SqlOperateTypes.GT, " > ");
        mysqlSyntax.put(SqlOperateTypes.LT, " < ");
        mysqlSyntax.put(SqlOperateTypes.GTE, " >= ");
        mysqlSyntax.put(SqlOperateTypes.LTE, " <= ");
        mysqlSyntax.put(SqlOperateTypes.OR, " OR ");
        SQL_POOL.put(DatasourceDriverTypes.MYSQL_DRIVER, mysqlSyntax);
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
