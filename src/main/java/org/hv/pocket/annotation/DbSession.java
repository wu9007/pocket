package org.hv.pocket.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库会话元数据
 *
 * @author leyan95 2019/1/31
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DbSession {
    /**
     * 要建立的数据库会话名称，默认为空字符时则取通过其他方式获取sessionName
     *
     * @return 要建立的数据库会话名称
     */
    String value() default "";

    /**
     * 是否开启事务，默认开启
     *
     * @return 是否开启事务
     */
    boolean tsOn() default true;
}
