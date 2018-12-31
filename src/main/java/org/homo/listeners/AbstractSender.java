package org.homo.listeners;

import org.homo.core.annotation.HomoMessage;
import org.homo.core.evens.ServiceEven;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/26
 */
public abstract class AbstractSender implements SmartApplicationListener {
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ServiceEven.class == eventType;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        Field field = (Field) source.get("field");
        Object result = source.get("result");
        HomoMessage messageAnnotation = field.getAnnotation(HomoMessage.class);
        if (messageAnnotation != null && messageAnnotation.open() && this.supportsType() == messageAnnotation.type()) {
            this.send(result);
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }

    /**
     * 支持的实体类
     *
     * @return 实体类类型
     */
    public abstract Class supportsType();

    /**
     * 发送消息
     *
     * @param object 注解所在函数处理返回的值
     */
    public abstract void send(Object object);
}
