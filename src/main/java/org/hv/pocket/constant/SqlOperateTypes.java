package org.hv.pocket.constant;

/**
 * @author wujianchuan 2019/1/10
 */
public interface SqlOperateTypes {
    String EQ = "equal";
    String NE = "notEqual";
    String LIKE = "like";
    String GT = "greaterThan";
    String LT = "lessThan";
    String GTE = "greaterThanOrEqual";
    String LTE = "lessThanOrEqual";
    String IS_NULL = "isNull";
    String IS_NOT_NULL = "isNotNull";
    String IN = "in";
    String NOT_IN = "notIn";
    String AND = "and";
    String OR = "or";
    String MAX = "max";
    String COUNT = "count";
    String DESC = "desc";
    String ASC = "asc";
    String TO_NUM = "toNum";
    String REGEXP = " regexp ";
    String NOW = "now";
    String LIMIT = " limit ";
}
