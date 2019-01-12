package org.homo.core.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wujianchuan 2019/1/12
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@org.springframework.stereotype.Repository
public @interface Repository {

    String database();

    String session();

    @AliasFor(annotation = Component.class)
    String value() default "";
}
