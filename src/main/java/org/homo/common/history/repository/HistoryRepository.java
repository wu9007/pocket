package org.homo.common.history.repository;

import org.homo.common.history.model.History;

/**
 * @author wujianchuan 2018/12/27
 */
public interface HistoryRepository {

    /**
     * 保存历史数据
     *
     * @param entity 实体
     * @return 影响行数
     */
    int save(History entity);
}
