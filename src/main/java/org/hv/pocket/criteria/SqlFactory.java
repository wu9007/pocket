package org.hv.pocket.criteria;

import org.hv.pocket.constant.DatasourceDriverTypes;
import org.hv.pocket.constant.SqlOperateTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author wujianchuan 2019/1/14
 */
public class SqlFactory {
    private static final Map<String, Map<String, String>> SQL_STR_POOL = new HashMap<>();
    private static final Map<String, Map<String, BiFunction<String, Object[], String>>> SQL_FUNCTION_POOL = new HashMap<>();

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
        sqlOperateTypes.put(SqlOperateTypes.REGEXP, " REGEXP ");

        //============================== MYSQL OPERATE ==============================//
        Map<String, String> mysqlOperateTypes = new HashMap<>(20);
        mysqlOperateTypes.put(SqlOperateTypes.TO_NUM, " CONVERT (?, SIGNED) ");
        mysqlOperateTypes.put(SqlOperateTypes.NOW, " NOW() ");

        Map<String, String> mysqlOperateDialect = new HashMap<>(20);
        mysqlOperateDialect.putAll(sqlOperateTypes);
        mysqlOperateDialect.putAll(mysqlOperateTypes);
        SQL_STR_POOL.put(DatasourceDriverTypes.MYSQL_DRIVER, mysqlOperateDialect);

        //============================== ORACLE OPERATE ==============================//
        Map<String, String> oracleOperateTypes = new HashMap<>(20);
        oracleOperateTypes.put(SqlOperateTypes.NOW, " TO_CHAR(SYSDATE,'yyyy-mm-dd hh24:mi:ss') FROM DUAL ");
        oracleOperateTypes.put(SqlOperateTypes.TO_NUM, " TO_NUMBER (?) ");

        Map<String, String> oracleOperateDialect = new HashMap<>(20);
        oracleOperateDialect.putAll(sqlOperateTypes);
        oracleOperateDialect.putAll(oracleOperateTypes);
        SQL_STR_POOL.put(DatasourceDriverTypes.ORACLE_DRIVER_OLD, oracleOperateDialect);
        SQL_STR_POOL.put(DatasourceDriverTypes.ORACLE_DRIVER, oracleOperateDialect);

        //============================== MYSQL FUNCTION ==============================//
        Map<String, BiFunction<String, Object[], String>> mysqlFunctionTypes = new HashMap<>(20);
        mysqlFunctionTypes.put(SqlOperateTypes.LIMIT, (sql, args) -> sql + " limit " + args[0] + ", " + args[1]);

        Map<String, BiFunction<String, Object[], String>> mysqlFunctionDialect = new HashMap<>(20);
        mysqlFunctionDialect.putAll(mysqlFunctionTypes);
        SQL_FUNCTION_POOL.put(DatasourceDriverTypes.MYSQL_DRIVER, mysqlFunctionDialect);


        //============================== ORACLE FUNCTION ==============================//
        Map<String, BiFunction<String, Object[], String>> oracleFunctionTypes = new HashMap<>(20);
        oracleFunctionTypes.put(SqlOperateTypes.LIMIT, (sql, args) -> {
            String selectStr = sql.substring(sql.toUpperCase().indexOf("SELECT") + 6, sql.toUpperCase().indexOf("FROM"));
            String tailStr = sql.substring(sql.toUpperCase().indexOf(" FROM"));
            String[] columns = selectStr.split(",");
            for (int index = 0; index < columns.length; index++) {
                String item = columns[index];
                if (item.contains(" AS ")) {
                    columns[index] = item.substring(item.indexOf(" AS ") + 4).replaceAll(" ", "");
                } else {
                    columns[index] = item.substring(item.indexOf(".") + 1).replaceAll(" ", "");
                }
            }
            String columnsSql = String.join(", ", columns);
            return "SELECT " + columnsSql + " FROM (SELECT " + columnsSql + ", ROWNUM AS POCKET_ROWNUM" + " FROM (SELECT " + selectStr + tailStr
                    + ")) WHERE POCKET_ROWNUM BETWEEN " + ((int) args[0] + 1) + " AND " + ((int) args[0] + (int) args[1]);
        });

        Map<String, BiFunction<String, Object[], String>> oracleFunctionDialect = new HashMap<>(20);
        oracleFunctionDialect.putAll(oracleFunctionTypes);
        SQL_FUNCTION_POOL.put(DatasourceDriverTypes.ORACLE_DRIVER_OLD, oracleFunctionDialect);
        SQL_FUNCTION_POOL.put(DatasourceDriverTypes.ORACLE_DRIVER, oracleFunctionDialect);
    }

    private static final SqlFactory OUR_INSTANCE = new SqlFactory();

    public static SqlFactory getInstance() {
        return OUR_INSTANCE;
    }

    private SqlFactory() {
    }

    public String getSql(String driverName, String dialect) {
        return SQL_STR_POOL.get(driverName).get(dialect);
    }

    public String applySql(String driverName, String dialect, String sql, Object[] args) {
        return SQL_FUNCTION_POOL.get(driverName).get(dialect).apply(sql, args);
    }
}
