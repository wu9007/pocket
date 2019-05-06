package org.hunter.pocket.criteria;

import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.AnnotationType;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.constant.SqlOperateTypes;
import org.hunter.pocket.exception.CriteriaException;
import org.hunter.pocket.model.MapperFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.hunter.pocket.exception.ErrorMessage.POCKET_ILLEGAL_FIELD_EXCEPTION;

/**
 * @author wujianchuan 2019/1/10
 */
public class Restrictions implements SqlBean {
    private final SqlFactory sqlFactory = SqlFactory.getInstance();

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

    private Restrictions(Object target) {
        this.target = target;
    }

    static Restrictions newParamInstance(Object target) {
        return new Restrictions(target);
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
        return new Restrictions(source, SqlOperateTypes.LT, target);
    }

    public static Restrictions gte(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.GTE, target);
    }

    public static Restrictions lte(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.LTE, target);
    }

    public static Restrictions like(String source, Object target) {
        return new Restrictions(source, SqlOperateTypes.LIKE, target);
    }

    public static Restrictions isNull(String source) {
        return new Restrictions(source, SqlOperateTypes.IS_NULL, null);
    }

    public static Restrictions isNotNull(String source) {
        return new Restrictions(source, SqlOperateTypes.IS_NOT_NULL, null);
    }

    public static Restrictions in(String source, List target) {
        return new Restrictions(source, SqlOperateTypes.IN, target);
    }

    public static Restrictions notIn(String source, List target) {
        return new Restrictions(source, SqlOperateTypes.NOT_IN, target);
    }

    public static Restrictions and(Restrictions leftRestrictions, Restrictions rightRestrictions) {
        return new Restrictions(leftRestrictions, SqlOperateTypes.AND, rightRestrictions);
    }

    public static Restrictions or(Restrictions leftRestrictions, Restrictions rightRestrictions) {
        return new Restrictions(leftRestrictions, SqlOperateTypes.OR, rightRestrictions);
    }

    String getSource() {
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

    private Restrictions getRightRestrictions() {
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
    String parseSql(Class clazz, DatabaseNodeConfig databaseConfig) {
        StringBuilder sql = new StringBuilder();
        try {
            if (this.getLeftRestrictions() == null) {
                if (AnnotationType.JOIN.equals(MapperFactory.getAnnotationType(clazz.getName(), this.getSource()))) {
                    Join join = (Join) MapperFactory.getAnnotation(clazz.getName(), this.getSource());
                    sql.append(join.joinTableSurname())
                            .append(CommonSql.DOT)
                            .append(join.destinationColumn())
                            .append(this.sqlFactory.getSql(databaseConfig.getDriverName(), this.getSqlOperate()));
                } else {
                    sql.append(MapperFactory.getTableName(clazz.getName()))
                            .append(CommonSql.DOT)
                            .append(MapperFactory.getRepositoryColumnName(clazz.getName(), this.getSource()))
                            .append(this.sqlFactory.getSql(databaseConfig.getDriverName(), this.getSqlOperate()));
                }
                if (this.getTarget() != null) {
                    if (SqlOperateTypes.IN.equals(this.getSqlOperate()) || SqlOperateTypes.NOT_IN.equals(this.getSqlOperate())) {
                        List targets = (List) this.getTarget();
                        sql.append((targets).stream().map(item -> CommonSql.PLACEHOLDER).collect(Collectors.joining(",", "(", ")")));
                    } else {
                        sql.append(CommonSql.PLACEHOLDER);
                    }
                }
            } else {
                sql.append(CommonSql.LEFT_BRACKET)
                        .append(this.getLeftRestrictions().parseSql(clazz, databaseConfig))
                        .append(this.sqlFactory.getSql(databaseConfig.getDriverName(), this.getSqlOperate()))
                        .append(this.getRightRestrictions().parseSql(clazz, databaseConfig))
                        .append(CommonSql.RIGHT_BRACKET);
            }
        } catch (NullPointerException e) {
            throw new CriteriaException(String.format(POCKET_ILLEGAL_FIELD_EXCEPTION, this.getSource()));
        }
        return sql.toString();
    }
}
