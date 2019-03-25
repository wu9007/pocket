package org.hunter.pocket.criteria;

import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public interface Criteria {

    /**
     * 添加约束条件
     *
     * @param restrictions 约束条件SQL
     * @return Criteria
     */
    Criteria add(Restrictions restrictions);

    /**
     * 添加更新实体
     *
     * @param modern 更新实体
     * @return Criteria
     */
    Criteria add(Modern modern);

    /**
     * 添加排序实体
     *
     * @param order 排序实体
     * @return Criteria
     */
    Criteria add(Sort order);

    /**
     * 获取分页数据
     *
     * @param start 起始位置
     * @param limit 数据条数
     * @return Criteria
     */
    Criteria limit(int start, int limit);

    /**
     * 获取所有数据
     *
     * @return 对象集合
     */
    List list();

    /**
     * 获取所有数据
     *
     * @param cascade 是否级联查询
     * @return 对象集合
     */
    List list(boolean cascade);

    /**
     * 获取最大值
     *
     * @param field 类型的属性
     * @return 返回对象
     */
    Object max(String field);

    /**
     * 查询总数
     *
     * @return long
     */
    long count();

    /**
     * 删除数据
     *
     * @return 影响行数
     */
    long delete();

    /**
     * 获取一条数据
     *
     * @return 对象
     */
    Object unique();

    /**
     * 获取一条数据
     *
     * @param cascade 是否级联查询
     * @return 对象
     */
    Object unique(boolean cascade);

    /**
     * 批量更新操作
     *
     * @return 影响行数
     */
    int update();
}
