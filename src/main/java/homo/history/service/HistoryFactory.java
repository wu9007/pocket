package homo.history.service;

import homo.common.model.Entity;
import homo.constant.OperateTypes;
import homo.history.model.History;
import homo.history.repository.HistoryRepositoryImpl;
import homo.repository.HomoRepository;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wujianchuan 2018/12/27
 */
public class HistoryFactory {
    private final static HistoryFactory INSTANCE = new HistoryFactory();

    private HomoRepository repository = new HistoryRepositoryImpl();

    private HistoryFactory() {
    }

    public static HistoryFactory getInstance() {
        return INSTANCE;
    }

    public boolean saveEntityHistory(Entity entity, Class clazz, OperateTypes operateType) {
        AtomicInteger effect = new AtomicInteger(0);
        String content = entity.getUuid();
        History history = History.newInstance(content, "Home", new Date());
        if (OperateTypes.SAVE.equals(operateType)) {
            effect.addAndGet(repository.save(history));
        } else if (OperateTypes.UPDATE.equals(operateType)) {
            effect.addAndGet(repository.update(history));
        } else if (OperateTypes.DELETE.equals(operateType)) {
            effect.addAndGet(repository.delete(history));
        }
        return effect.get() > 0;
    }
}
