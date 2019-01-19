package org.hunter.pocket.criteria;

import org.hunter.pocket.constant.SqlOperateTypes;

/**
 * @author wujianchuan 2019/1/10
 */
public class Restrictions {
    private String source;
    private String sqlOperate;
    private Object target;
    private Restrictions leftRestrictions;
    private Restrictions rightRestrictions;

    private Restrictions(String source, String sqlOperate, Object target) {
        this.source = source;
        this.sqlOperate = sqlOperate;
        this.target = target;
    }

    private Restrictions(Restrictions leftRestrictions, String sqlOperate, Restrictions rightRestrictions) {
        this.leftRestrictions = leftRestrictions;
        this.sqlOperate = sqlOperate;
        this.rightRestrictions = rightRestrictions;
    }

    public static Restrictions equ(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.EQ, target);
    }

    public static Restrictions ne(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.NE, target);
    }

    public static Restrictions gt(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.GT, target);
    }

    public static Restrictions lt(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.LTE, target);
    }

    public static Restrictions gte(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.GT, target);
    }

    public static Restrictions lte(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.LTE, target);
    }

    public static Restrictions like(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.LIKE, target);
    }

    public static Restrictions and(Restrictions leftRestrictions, Restrictions rightRestrictions) {
        return new Restrictions(leftRestrictions, SqlOperateTypes.AND, rightRestrictions);
    }

    public static Restrictions or(Restrictions leftRestrictions, Restrictions rightRestrictions) {
        return new Restrictions(leftRestrictions, SqlOperateTypes.OR, rightRestrictions);
    }

    public String getSource() {
        return source;
    }

    String getSqlOperate() {
        return sqlOperate;
    }

    public Object getTarget() {
        return target;
    }

    Restrictions getLeftRestrictions() {
        return leftRestrictions;
    }

    Restrictions getRightRestrictions() {
        return rightRestrictions;
    }
}
