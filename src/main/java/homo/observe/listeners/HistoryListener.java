package homo.observe.listeners;

import homo.common.model.Entity;
import homo.constant.OperateTypes;
import homo.history.service.HistoryFactory;
import homo.observe.evens.ModelSaveEven;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author wujianchuan 2018/12/26
 */
@Component
public class HistoryListener implements SmartApplicationListener {
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ModelSaveEven.class == eventType;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        Class clazz = (Class) source.get("clazz");
        Entity entity = (Entity) source.get("entity");

        boolean success = HistoryFactory.getInstance().saveEntityHistory(entity, clazz, OperateTypes.SAVE);
        if (success) {
            System.out.println("历史保存成功。");
        } else {
            System.out.println("历史保存失败");
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
