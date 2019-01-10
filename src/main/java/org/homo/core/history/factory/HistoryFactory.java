package org.homo.core.history.factory;

import org.homo.authority.model.User;
import org.homo.core.model.BaseEntity;
import org.homo.core.constant.LogicOperateTypes;
import org.homo.core.history.model.History;
import org.homo.core.history.repository.HistoryRepositoryImpl;

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

    public void saveEntityHistory(BaseEntity entity, Class clazz, LogicOperateTypes operateType, User operator) {

        StringBuilder content = new StringBuilder("【")
                .append(operateType.getName())
                .append("】uuid：")
                .append(entity.getUuid());

        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !"serialVersionUID".equals(field.getName()))
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
