package org.hunter.pocket.annotation;

import org.hunter.pocket.constant.CacheTarget;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    /**
     * This is the cached index keyword
     *
     * @return keyword
     */
    String key();

    /**
     * The unit is milliseconds
     *
     * @return the suggested duration 10 milliseconds, if any (or empty value otherwise)
     */
    long duration() default 10;

    /**
     * 缓存位置
     *
     * @return CacheTarget
     */
    CacheTarget target() default CacheTarget.DATA_BASE;
}
