package org.homo.common.history.factory;

import org.homo.authority.model.User;
import org.homo.common.model.BaseEntity;
import org.homo.common.constant.OperateTypes;
import org.homo.common.history.model.History;
import org.homo.common.history.repository.HistoryRepositoryImpl;

import java.util.Arrays;
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

        StringBuilder content = new StringBuilder("【")
                .append(operateType.getName())
                .append("】uuid：")
                .append(entity.getUuid());
        Arrays.stream(clazz.getDeclaredFields()).filter(field -> !"serialVersionUID".equals(field.getName()))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        content.append("\t")
                                .append(field.getName())
                                .append("：")
                                .append(field.get(entity));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
        History history = History.newInstance(content.toString(), operator.getName(), new Date());
        repository.save(history);
    }
}
