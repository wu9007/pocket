package org.homo.pocket.criteria;

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
}
