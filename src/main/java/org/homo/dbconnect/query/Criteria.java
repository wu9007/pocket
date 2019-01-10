package org.homo.dbconnect.query;

import org.homo.core.model.BaseEntity;

import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public interface Criteria {

    /**
     * 添加约束条件
     *
     * @param restrictionsSql 约束条件SQL
     */
    void add(String restrictionsSql);

    /**
     * 获取所有数据
     *
     * @return 对象集合
     */
    List<BaseEntity> list();

    /**
     * 获取一条数据
     *
     * @return 对象
     */
    BaseEntity unique();
}
