package org.hunter.pocket.criteria;

import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.constant.SqlOperateTypes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wujianchuan
 */
public class SqlBody {

    private String tableName;
    private List<String> columnNames;
    private List<Modern> modernList;
    private List<Join> tableJoins;
    private List<Restrictions> restrictionsList;
    private List<Sort> orderList;

    private SqlBody(String tableName, List<String> columnNames, List<Join> tableJoins, List<Restrictions> restrictionsList, List<Modern> modernList, List<Sort> orderList) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.tableJoins = tableJoins;
        this.restrictionsList = restrictionsList;
        this.modernList = modernList;
        this.orderList = orderList;
    }

    public static SqlBody newInstance(String tableName, List<String> columnNames, List<Join> tableJoins, List<Restrictions> restrictions, List<Modern> modernList, List<Sort> orderList) {
        return new SqlBody(tableName, columnNames, tableJoins, restrictions, modernList, orderList);
    }

    String buildSelectSql(Map<String, FieldMapper> fieldMapper, DatabaseNodeConfig databaseConfig) {
        return CommonSql.SELECT +
                this.parseColumnSql() +
                CommonSql.FROM +
                this.tableName +
                this.parseJoinSql() +
                this.parseRestrictionsSql(fieldMapper, databaseConfig) +
                this.parseOrderSql(fieldMapper);
    }

    String buildUpdateSql(Map<String, FieldMapper> fieldMapper, List<ParameterTranslator> parameters, Map<String, Object> parameterMap, DatabaseNodeConfig databaseConfig) {
        return CommonSql.UPDATE +
                this.tableName +
                CommonSql.SET +
                this.parserModernSql(fieldMapper, parameters, parameterMap) +
                this.parseRestrictionsSql(fieldMapper, databaseConfig);

    }

    String buildCountSql(Map<String, FieldMapper> fieldMapper, DatabaseNodeConfig databaseConfig) {
        return CommonSql.SELECT +
                SqlOperateTypes.COUNT +
                "(0)" +
                CommonSql.FROM +
                this.tableName +
                this.parseRestrictionsSql(fieldMapper, databaseConfig);
    }

    String buildDeleteSql(Map<String, FieldMapper> fieldMapper, DatabaseNodeConfig databaseConfig) {
        return CommonSql.DELETE +
                CommonSql.FROM +
                this.tableName +
                this.parseRestrictionsSql(fieldMapper, databaseConfig);
    }

    String buildMaxSql(Map<String, FieldMapper> fieldMapper, DatabaseNodeConfig databaseConfig, String fieldName) {
        return CommonSql.SELECT +
                SqlOperateTypes.MAX +
                "(" + fieldMapper.get(fieldName).getColumnName() + ")" +
                CommonSql.FROM +
                this.tableName +
                this.parseRestrictionsSql(fieldMapper, databaseConfig);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<Modern> getModernList() {
        return modernList;
    }

    public void setModernList(List<Modern> modernList) {
        this.modernList = modernList;
    }

    public List<Join> getTableJoins() {
        return tableJoins;
    }

    public void setTableJoins(List<Join> tableJoins) {
        this.tableJoins = tableJoins;
    }

    public List<Restrictions> getRestrictionsList() {
        return restrictionsList;
    }

    public void setRestrictionsList(List<Restrictions> restrictionsList) {
        this.restrictionsList = restrictionsList;
    }

    public List<Sort> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Sort> orderList) {
        this.orderList = orderList;
    }

    private String parseColumnSql() {
        return String.join(",", this.columnNames);
    }

    private String parserModernSql(Map<String, FieldMapper> fieldMapper, List<ParameterTranslator> parameters, Map<String, Object> parameterMap) {
        return this.modernList.stream()
                .map(modern -> modern.parse(fieldMapper, parameters, parameterMap))
                .collect(Collectors.joining(", "));
    }

    private String parseJoinSql() {
        return this.tableJoins.stream()
                .map(join -> {
                    String joinTableSurname = join.joinTableSurname().trim();
                    return new StringBuilder(join
                            .joinMethod().getId())
                            .append(" ")
                            .append(join.joinTable())
                            .append(" ")
                            .append(joinTableSurname)
                            .append(CommonSql.ON)
                            .append(this.tableName)
                            .append(".")
                            .append(join.columnName())
                            .append(" = ")
                            .append(joinTableSurname.length() > 0 ? joinTableSurname : join.joinTable())
                            .append(".")
                            .append(join.bridgeColumn());
                })
                .collect(Collectors.joining());
    }

    private String parseRestrictionsSql(Map<String, FieldMapper> fieldMapper, DatabaseNodeConfig databaseConfig) {
        List<String> restrictionSqlList = new LinkedList<>();
        for (Restrictions restrictions : this.restrictionsList) {
            restrictionSqlList.add(restrictions.parseSql(this.tableName, fieldMapper, databaseConfig));
        }
        return restrictionSqlList.size() > 0 ? CommonSql.WHERE + String.join(CommonSql.AND, restrictionSqlList) : "";
    }

    private String parseOrderSql(Map<String, FieldMapper> fieldMapper) {
        if (this.orderList != null && this.orderList.size() > 0) {
            return CommonSql.ORDER_BY +
                    this.orderList.stream()
                            .map(order -> fieldMapper.get(order.getSource()).getColumnName() + " " + order.getSortType())
                            .collect(Collectors.joining(","));
        } else {
            return "";
        }
    }
}
