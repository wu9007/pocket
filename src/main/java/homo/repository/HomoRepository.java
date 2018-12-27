package homo.repository;

import homo.common.model.Entity;

/**
 * @author wujianchuan 2018/12/26
 */
public interface HomoRepository<T extends Entity> {

    /**
     * 保存实体
     * @param entity 实体类
     * @return 影响行数
     */
    int save(T entity);

    /**
     * 更新实体
     * @param entity 实体类
     * @return 影响行数
     */
    int update(T entity);

    /**
     * 删除实体
     * @param entity 实体类
     * @return 影响行数
     */
    int delete(T entity);
}
