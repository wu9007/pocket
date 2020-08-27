package org.hv.pocket.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wujianchuan 2019/1/9
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ManyToOne {
    /**
     * 列名
     *
     * @return column name
     */
    String columnName() default "";

    /**
     * 关联主类类类型
     *
     * @return 主类类类型
     */
    Class<?> clazz();

    /**
     * 主类关联属性名
     *
     * @return up bridge field.
     */
    String upBridgeField();

    /**
     * 在更新数据比较实体获取脏数据时是否忽略该属性（默认不忽略）
     *
     * @return ignore to compare or not
     */
    boolean ignoreCompare() default false;
}
