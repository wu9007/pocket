package org.hv.pocket.annotation;

import org.hv.pocket.identify.GenerationType;

import java.lang.annotation.*;

/**
 * @author wujianchuan 2019/1/3
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Identify {
    String strategy() default GenerationType.STR_INCREMENT;
}
