package homo.repository;

import homo.model.Entity;

/**
 * @author wujianchuan 2018/12/26
 */
public interface AbcRepository {

    /**
     * 保存实体
     * @param entity 实体类
     * @return 影响行数
     */
    int save(Entity entity);

    /**
     * 更新实体
     * @param entity 实体类
     * @return 影响行数
     */
    int update(Entity entity);

    /**
     * 删除实体
     * @param entity 实体类
     * @return 影响行数
     */
    int delete(Entity entity);
}
