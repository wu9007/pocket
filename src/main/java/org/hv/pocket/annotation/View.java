package org.hv.pocket.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author wujianchuan 2018/12/28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Entity(table = "")
public @interface View {
}
