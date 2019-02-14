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
     * 获取所有数据
     *
     * @return 对象集合
     * @throws Exception 异常
     */
    List list() throws Exception;

    /**
     * 获取所有数据
     *
     * @param cascade 是否级联查询
     * @return 对象集合
     * @throws Exception 异常
     */
    List list(boolean cascade) throws Exception;

    /**
     * 获取最大值
     *
     * @param field 类型的属性
     * @return 返回对象
     * @throws Exception e
     */
    Object max(String field) throws Exception;

    /**
     * 查询总数
     *
     * @return long
     * @throws Exception e
     */
    long count() throws Exception;

    /**
     * 删除数据
     *
     * @return 影响行数
     * @throws Exception e
     */
    long delete() throws Exception;

    /**
     * 获取一条数据
     *
     * @return 对象
     * @throws Exception 异常
     */
    Object unique() throws Exception;

    /**
     * 获取一条数据
     *
     * @param cascade 是否级联查询
     * @return 对象
     * @throws Exception 异常
     */
    Object unique(boolean cascade) throws Exception;

    /**
     * 批量更新操作
     *
     * @return 影响行数
     * @throws Exception 异常
     */
    int update() throws Exception;
}
