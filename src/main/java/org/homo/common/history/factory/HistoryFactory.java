package org.homo.common.history.factory;

import org.homo.authority.model.User;
import org.homo.common.model.BaseEntity;
import org.homo.common.constant.OperateTypes;
import org.homo.common.history.model.History;
import org.homo.common.history.repository.HistoryRepositoryImpl;

import java.util.Date;

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

    public void saveEntityHistory(BaseEntity entity, Class clazz, OperateTypes operateType, User operator) {
        String content = operateType.getName() + "了数据标识为：" + entity.getUuid() + "的数据。";
        History history = History.newInstance(content, operator.getName(), new Date());
        repository.save(history);
    }
}
