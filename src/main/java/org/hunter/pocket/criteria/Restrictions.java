package org.hunter.pocket.criteria;

import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.SqlOperateTypes;
import org.hunter.pocket.exception.CriteriaException;

import java.util.List;
import java.util.Map;

import static org.hunter.pocket.exception.ErrorMessage.POCKET_ILLEGAL_FIELD_EXCEPTION;

/**
 * @author wujianchuan 2019/1/10
 */
public class Restrictions implements SqlBean {
    private final SqlFactory sqlFactory = SqlFactory.getInstance();

    private String source;
    private final String sqlOperate;
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

    @Override
    public Object getTarget() {
        return target;
    }

    Restrictions getLeftRestrictions() {
        return leftRestrictions;
    }

    Restrictions getRightRestrictions() {
        return rightRestrictions;
    }

    void pushTo(List<Restrictions> restrictionsList) {
        if (this.getLeftRestrictions() != null && this.getRightRestrictions() != null) {
            this.getLeftRestrictions().pushTo(restrictionsList);
            this.getRightRestrictions().pushTo(restrictionsList);
        } else {
            restrictionsList.add(this);
        }
    }

    /**
     * 解析出SQL
     *
     * @return SQL
     */
    String parseSql(String tableName, Map<String, FieldMapper> fieldMapper, DatabaseNodeConfig databaseConfig) {
        StringBuilder sql = new StringBuilder();
        try {

            if (this.getLeftRestrictions() == null) {
                sql.append(tableName)
                        .append(".")
                        .append(fieldMapper.get(this.getSource()).getColumnName())
                        .append(this.sqlFactory.getSql(databaseConfig.getDriverName(), this.getSqlOperate()))
                        .append("?");
            } else {
                sql.append("(")
                        .append(this.getLeftRestrictions().parseSql(tableName, fieldMapper, databaseConfig))
                        .append(this.sqlFactory.getSql(databaseConfig.getDriverName(), this.getSqlOperate()))
                        .append(this.getRightRestrictions().parseSql(tableName, fieldMapper, databaseConfig))
                        .append(")");
            }
        } catch (NullPointerException e) {
            throw new CriteriaException(String.format(POCKET_ILLEGAL_FIELD_EXCEPTION, this.getSource()));
        }
        return sql.toString();
    }
}
