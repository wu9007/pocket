package org.homo.dbconnect;

import org.homo.core.model.BaseEntity;

import java.sql.SQLException;

/**
 * @author wujianchuan 2018/12/31
 */

interface Session {

    /**
     * 获取事务对象
     *
     * @return 事务对象
     */
    Transaction getTransaction();

    /**
     * 保存实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int save(BaseEntity entity);

    /**
     * 更新实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int update(BaseEntity entity);

    /**
     * 删除实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int delete(BaseEntity entity);

    /**
     * 查询实体
     *
     * @param uuid 数据标识
     * @return 实体对象
     */
    BaseEntity findOne(String uuid);
}
