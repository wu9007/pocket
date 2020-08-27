package org.hv.pocket.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wujianchuan 2020/8/26 08:39
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneToOne {
    /**
     * 在本类中对应关联列的属性
     *
     * @return Own Field
     */
    String ownField();

    /**
     * 被关联的类型属性名
     *
     * @return Related Field
     */
    String relatedField();
}
