package org.hunter.pocket.annotation;

import org.hunter.pocket.constant.JoinMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wujianchuan 2019/1/3
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Join {
    /**
     * 数据库字段名
     *
     * @return Column Name
     */
    String columnName();

    /**
     * 业务名称
     *
     * @return Business Name
     */
    String businessName() default "";

    /**
     * 关联的数据库表
     *
     * @return Join Table
     */
    String joinTable();

    /**
     * 关联方式
     *
     * @return Join Method
     */
    JoinMethod joinMethod();

    /**
     * 关联数据库字段
     *
     * @return Bridge Column Name
     */
    String bridgeColumn();

    /**
     * 关联数据库字段
     *
     * @return Column Value Name
     */
    String destinationColumn();
}
