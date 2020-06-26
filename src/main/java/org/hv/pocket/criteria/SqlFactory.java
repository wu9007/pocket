package org.hv.pocket.criteria;

import org.hv.pocket.constant.DatasourceDriverTypes;
import org.hv.pocket.constant.SqlOperateTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/14
 */
public class SqlFactory {
    private static final Map<String, Map<String, String>> SQL_POOL = new HashMap<>();

    static {
        Map<String, String> sqlOperateTypes = new HashMap<>(20);
        sqlOperateTypes.put(SqlOperateTypes.AND, " AND ");
        sqlOperateTypes.put(SqlOperateTypes.EQ, " = ");
        sqlOperateTypes.put(SqlOperateTypes.NE, " <> ");
        sqlOperateTypes.put(SqlOperateTypes.LIKE, " LIKE ");
        sqlOperateTypes.put(SqlOperateTypes.GT, " > ");
        sqlOperateTypes.put(SqlOperateTypes.LT, " < ");
        sqlOperateTypes.put(SqlOperateTypes.GTE, " >= ");
        sqlOperateTypes.put(SqlOperateTypes.LTE, " <= ");
        sqlOperateTypes.put(SqlOperateTypes.IS_NULL, " IS NULL ");
        sqlOperateTypes.put(SqlOperateTypes.IS_NOT_NULL, " IS NOT NULL ");
        sqlOperateTypes.put(SqlOperateTypes.IN, " IN ");
        sqlOperateTypes.put(SqlOperateTypes.NOT_IN, " NOT IN ");
        sqlOperateTypes.put(SqlOperateTypes.OR, " OR ");
        sqlOperateTypes.put(SqlOperateTypes.CONVERT, " CONVERT ");
        sqlOperateTypes.put(SqlOperateTypes.SIGNED, " SIGNED ");
        sqlOperateTypes.put(SqlOperateTypes.REGEXP, " REGEXP ");

        Map<String, String> mySqlOperateTypes = new HashMap<>(20);
        mySqlOperateTypes.putAll(sqlOperateTypes);
        SQL_POOL.put(DatasourceDriverTypes.MYSQL_DRIVER, mySqlOperateTypes);

        Map<String, String> oracleSqlOperateTypes = new HashMap<>(20);
        oracleSqlOperateTypes.putAll(mySqlOperateTypes);
        SQL_POOL.put(DatasourceDriverTypes.ORACLE_DRIVER_OLD, oracleSqlOperateTypes);
        SQL_POOL.put(DatasourceDriverTypes.ORACLE_DRIVER, oracleSqlOperateTypes);
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
