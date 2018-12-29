package org.homo.common.message.listeners;

import org.homo.common.annotation.HomoMessage;
import org.homo.common.evens.ServiceEven;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/26
 */
@Component
public class MessageListener implements SmartApplicationListener {
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
        if (messageAnnotation != null && messageAnnotation.open()) {
            System.out.println("发送短信-" + result);
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
