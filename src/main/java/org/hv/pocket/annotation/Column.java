package org.hv.pocket.annotation;

import org.hv.pocket.constant.EncryptType;
import org.omg.CORBA.UNKNOWN;

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
public @interface Column {
    /**
     * 对应数据库列明(缺省时根据属性名驼峰转下划线)
     *
     * @return column name
     */
    String name() default "";

    String businessName() default "";

    /**
     * 关键业务
     *
     * @return flag business
     */
    boolean flagBusiness() default false;

    /**
     * 在更新数据比较实体获取脏数据时是否忽略该属性（默认不忽略）
     *
     * @return ignore to compare or not
     */
    boolean ignoreCompare() default false;

    /**
     * 加密存储的加密方式{@link EncryptType}
     *
     * @return 加密方式
     */
    String encryptMode() default "";
}
