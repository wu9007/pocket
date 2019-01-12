package org.homo.dbconnect.criteria;

/**
 * @author wujianchuan 2019/1/10
 */
public class Restrictions {
    private String source;
    private String restrictionsSQL;
    private Object target;

    private Restrictions(String source, String restrictionsSQL, Object target) {
        this.source = source;
        this.restrictionsSQL = restrictionsSQL;
        this.target = target;
    }

    public static Restrictions equ(String source, Object target) {
        String sql = "$" + source + " = ?";
        return new Restrictions(source, sql, target);
    }
}
