package homo.observe.listeners;

import homo.observe.evens.EntityRepositoryEven;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author wujianchuan 2018/12/26
 */
@Component
public class MessageListener implements SmartApplicationListener {
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return EntityRepositoryEven.class == eventType;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("发送短信。");
    }
}
