package org.homo.dbconnect.criteria;

import org.homo.dbconnect.constant.DatasourceDriverTypes;
import org.homo.dbconnect.constant.SqlOperateTypes;

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
        mysqlSyntax.put(SqlOperateTypes.OR, " OR ");
        SQL_POOL.put(DatasourceDriverTypes.MYSQL_DRIVER, mysqlSyntax);
    }

    private static SqlFactory ourInstance = new SqlFactory();

    public static SqlFactory getInstance() {
        return ourInstance;
    }

    private SqlFactory() {
    }

    String getSql(String driverName, String sqlOperateType) {
        return SQL_POOL.get(driverName).get(sqlOperateType);
    }
}
