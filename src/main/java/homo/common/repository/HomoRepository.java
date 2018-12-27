package homo.common.repository;

import homo.authority.model.User;
import homo.common.model.BaseEntity;

/**
 * @author wujianchuan 2018/12/26
 */
public interface HomoRepository<T extends BaseEntity> {

    /**
     * 保存实体
     * @param entity 实体类
     * @return 影响行数
     */
    int save(T entity, User operator);

    /**
     * 更新实体
     * @param entity 实体类
     * @return 影响行数
     */
    int update(T entity, User operator);

    /**
     * 删除实体
     * @param entity 实体类
     * @return 影响行数
     */
    int delete(T entity, User operator);
}
