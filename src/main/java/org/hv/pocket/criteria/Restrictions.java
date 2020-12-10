package org.hv.pocket.criteria;

import org.hv.pocket.annotation.Join;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.AnnotationType;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.constant.SqlOperateTypes;
import org.hv.pocket.exception.CriteriaException;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.hv.pocket.exception.ErrorMessage.POCKET_ILLEGAL_FIELD_EXCEPTION;

/**
 * @author wujianchuan 2019/1/10
 */
public class Restrictions implements SqlBean {
    private final SqlFactory sqlFactory = SqlFactory.getInstance();

    private String source;
    private String sqlOperate;
    private Object target;
    private boolean encrypted;
    /**
     * The 'OR' or 'AND' {@link SqlOperateTypes} is applied to this property
     */
    private Restrictions[] restrictions;

    private Restrictions(String source, String sqlOperate, Object target) {
        this.source = source;
        this.sqlOperate = sqlOperate;
        if (target == null && !SqlOperateTypes.IS_NULL.equals(sqlOperate) && !SqlOperateTypes.IS_NOT_NULL.equals(sqlOperate)) {
            throw new CriteriaException(String.format("Parameter <<%s>> can not be null, please use method Restrictions.isNull(source) when you want to judge whether " +
                    "it's empty or not", source));
        }
        this.target = target;
    }

    private Restrictions(Restrictions[] restrictions, String sqlOperate) {
        this.sqlOperate = sqlOperate;
        int minItemCount = 2;
        if (restrictions.length < minItemCount) {
            throw new CriteriaException("This operation requires at least two elements.");
        }
        this.restrictions = restrictions;
    }

    private Restrictions(Object target) {
        this.target = target;
    }

    private static Restrictions newParamInstance(Object target) {
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

    public static Restrictions in(String source, List<?> target) {
        if (target == null || target.size() == 0) {
            throw new CriteriaException("The <<in>> function of Restrictions cannot be called with an empty argument.");
        }
        return new Restrictions(source, SqlOperateTypes.IN, target);
    }

    public static Restrictions notIn(String source, List<?> target) {
        if (target == null || target.size() == 0) {
            throw new CriteriaException("The <<notIn>> function of Restrictions cannot be called with an empty argument.");
        }
        return new Restrictions(source, SqlOperateTypes.NOT_IN, target);
    }

    public static Restrictions and(Restrictions leftRestrictions, Restrictions rightRestrictions) {
        Restrictions[] restrictions = {leftRestrictions, rightRestrictions};
        return Restrictions.and(restrictions);
    }

    public static Restrictions and(Restrictions... orRestrictionsList) {
        return new Restrictions(orRestrictionsList, SqlOperateTypes.AND);
    }

    public static Restrictions or(Restrictions leftRestrictions, Restrictions rightRestrictions) {
        Restrictions[] restrictions = {leftRestrictions, rightRestrictions};
        return Restrictions.or(restrictions);
    }

    public static Restrictions or(Restrictions... orRestrictionsList) {
        return new Restrictions(orRestrictionsList, SqlOperateTypes.OR);
    }

    @Override
    public String getSource() {
        return source;
    }

    String getSqlOperate() {
        return sqlOperate;
    }

    public Restrictions[] getRestrictions() {
        return restrictions;
    }

    @Override
    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public boolean getEncrypted() {
        return encrypted;
    }

    @Override
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    void pushTo(List<Restrictions> sortedRestrictionsList) {
        if (this.restrictions != null) {
            for (Restrictions restrictions : this.restrictions) {
                restrictions.pushTo(sortedRestrictionsList);
            }
        } else {
            if (!SqlOperateTypes.IS_NULL.equals(this.getSqlOperate()) && !SqlOperateTypes.IS_NOT_NULL.equals(this.getSqlOperate())) {
                if (this.getTarget() instanceof List) {
                    List<?> targets = ((List<?>) this.getTarget());
                    for (Object item : targets) {
                        Restrictions.newParamInstance(item).pushTo(sortedRestrictionsList);
                    }
                } else {
                    sortedRestrictionsList.add(this);
                }
            }
        }
    }

    /**
     * 解析出SQL
     *
     * @return SQL
     */
    String parseSql(Class<? extends AbstractEntity> clazz, DatabaseNodeConfig databaseConfig) {
        StringBuilder sql = new StringBuilder();
        try {
            if (this.restrictions == null) {
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
                        List<?> targets = (List<?>) this.getTarget();
                        sql.append((targets).stream().map(item -> CommonSql.PLACEHOLDER).collect(Collectors.joining(",", "(", ")")));
                    } else {
                        sql.append(CommonSql.PLACEHOLDER);
                    }
                }
            } else {
                sql.append(CommonSql.LEFT_BRACKET);
                for (int index = 0; index < restrictions.length; index++) {
                    if (index != 0) {
                        sql.append(this.sqlFactory.getSql(databaseConfig.getDriverName(), this.getSqlOperate()));
                    }
                    sql.append(restrictions[index].parseSql(clazz, databaseConfig));
                }
                sql.append(CommonSql.RIGHT_BRACKET);
            }
        } catch (NullPointerException e) {
            throw new CriteriaException(String.format(POCKET_ILLEGAL_FIELD_EXCEPTION, this.getSource()));
        }
        return sql.toString();
    }
}
