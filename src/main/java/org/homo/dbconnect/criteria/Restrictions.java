package org.homo.dbconnect.criteria;

import org.homo.dbconnect.constant.SqlOperateTypes;

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

    public static Restrictions and(Restrictions leftRestrictions, Restrictions rightRestrictions) {
        return new Restrictions(leftRestrictions, SqlOperateTypes.AND, rightRestrictions);
    }

    String getSource() {
        return source;
    }

    String getSqlOperate() {
        return sqlOperate;
    }

    Object getTarget() {
        return target;
    }

    Restrictions getLeftRestrictions() {
        return leftRestrictions;
    }

    Restrictions getRightRestrictions() {
        return rightRestrictions;
    }
}
