package org.hunter.pocket.criteria;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.constant.DatasourceDriverTypes;
import org.hunter.pocket.constant.SqlOperateTypes;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    /**
     * 获取sql体
     *
     * @param mainTableName    主表名
     * @param fields           属性集合
     * @param restrictionsList 条件集合
     * @param modernList       更新数据集合
     * @return SQL Body 对象。
     */
    SqlBody getSqlBody(String mainTableName, Field[] fields, List<Restrictions> restrictionsList, List<Modern> modernList, List<Sort> orderList) {
        List<String> columnNames = new LinkedList<>();
        List<Join> tableJoins = new LinkedList<>();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            Join join = field.getAnnotation(Join.class);
            if (column != null) {
                columnNames.add(mainTableName + "." + column.name());
            } else if (manyToOne != null) {
                columnNames.add(mainTableName + "." + manyToOne.columnName());
            } else if (join != null) {
                String joinTableSurname = join.joinTableSurname().trim();
                String tableName = joinTableSurname.length() > 0 ? joinTableSurname : join.joinTable();
                columnNames.add(tableName + "." + join.destinationColumn() + CommonSql.AS + join.columnName());
                tableJoins.add(join);
            } else {
                throw new NullPointerException("找不到注解");
            }
        }
        return SqlBody.newInstance(mainTableName, columnNames, tableJoins, restrictionsList, modernList, orderList);
    }
}
