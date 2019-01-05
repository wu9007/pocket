package org.homo.core.repository;

import org.homo.authority.model.User;
import org.homo.core.model.BaseEntity;

import java.sql.SQLException;

/**
 * @author wujianchuan 2018/12/26
 */
public interface HomoRepository<T extends BaseEntity> {

    /**
     * 保存实体
     * @param entity 实体类
     * @param operator 操作人
     * @return 影响行数
     */
    T save(T entity, User operator) throws SQLException, IllegalAccessException;

    /**
     * 更新实体
     * @param entity 实体类
     * @param operator 操作人
     * @return 影响行数
     */
    T update(T entity, User operator);

    /**
     * 删除实体
     * @param entity 实体类
     * @param operator 操作人
     * @return 影响行数
     */
    int delete(T entity, User operator);
}
