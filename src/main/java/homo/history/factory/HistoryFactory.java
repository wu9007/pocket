package homo.history.factory;

import homo.User;
import homo.common.model.Entity;
import homo.constant.OperateTypes;
import homo.history.model.History;
import homo.history.repository.HistoryRepositoryImpl;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wujianchuan 2018/12/27
 */
public class HistoryFactory {
    private final static HistoryFactory INSTANCE = new HistoryFactory();

    private HistoryRepositoryImpl repository = new HistoryRepositoryImpl();

    private HistoryFactory() {
    }

    public static HistoryFactory getInstance() {
        return INSTANCE;
    }

    public boolean saveEntityHistory(Entity entity, Class clazz, OperateTypes operateType, User operator) {
        AtomicInteger effect = new AtomicInteger(0);
        String content = operateType.getName() + "了数据标识为：" + entity.getUuid() + "的数据。";
        History history = History.newInstance(content, operator.getName(), new Date());
        effect.addAndGet(repository.save(history));

        return effect.get() > 0;
    }
}
