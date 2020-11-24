package org.hv.pocket.annotation;

import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.constant.JoinMethod;

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
    String columnName() default "";

    /**
     * 数据库字段别名，查询时使用，不用于保存操作
     *
     * @return column sur name
     */
    String columnSurname();

    /**
     * 业务名称
     *
     * @return Business Name
     */
    String businessName() default "";

    /**
     * 关键业务
     *
     * @return flag business
     */
    boolean flagBusiness() default false;

    /**
     * 关联的数据库表
     *
     * @return Join Table
     */
    String joinTable();

    /**
     * 关联表别名
     *
     * @return join table surname
     */
    String joinTableSurname();

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
     * 关联查询出的属性值，作为最终结果赋值给字段
     *
     * @return Column Value Name
     */
    String destinationColumn();

    /**
     * 关联查询数据时使用的解密方式{@link EncryptType}
     *
     * @return 解密方式
     */
    String encryptMode() default "";
}
