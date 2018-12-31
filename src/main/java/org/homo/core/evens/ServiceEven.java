package org.homo.core.evens;

import org.springframework.context.ApplicationEvent;

/**
 * @author wujianchuan 2018/12/26
 */
public class ServiceEven extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ServiceEven(Object source) {
        super(source);
    }
}
