package org.hv.pocket.annotation;

import org.hv.pocket.model.AbstractEntity;

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
public @interface OneToMany {
    /**
     * 关联的类类型
     *
     * @return Clazz
     */
    Class<? extends AbstractEntity> clazz();

    /**
     * 关联的类型属性
     *
     * @return Bridge Field
     */
    String bridgeField();

    String businessName() default "";

    /**
     * 关键业务
     *
     * @return flag business
     */
    boolean flagBusiness() default false;
}
