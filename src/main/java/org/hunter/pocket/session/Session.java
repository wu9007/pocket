package org.hunter.pocket.session;

import org.hunter.pocket.model.BaseEntity;
import org.hunter.pocket.session.actions.BuildDictionary;
import org.hunter.pocket.session.actions.BuildTransaction;
import org.hunter.pocket.session.actions.OperateDictionary;

/**
 * @author wujianchuan 2018/12/31
 */

public interface Session extends BuildDictionary, OperateDictionary, BuildTransaction {

    /**
     * 删除该数据的缓存
     *
     * @param entity 实体对象
     */
    void removeCache(BaseEntity entity);
}
