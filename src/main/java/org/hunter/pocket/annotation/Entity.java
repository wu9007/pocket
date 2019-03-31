package org.hunter.pocket.annotation;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wujianchuan 2018/12/28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Bean
@Component
public @interface Entity {
    String table();

    int tableId();

    String uuidGenerator() default "long_increment";

    String businessName() default "";
}
