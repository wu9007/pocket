package org.hv.pocket.criteria;

import org.hv.pocket.criteria.actions.OperateDictionary;

/**
 * @author wujianchuan 2019/1/10
 */
public interface Criteria extends OperateDictionary {

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
     * 参数赋值
     *
     * @param key   键
     * @param value 值
     * @return Criteria
     */
    Criteria setParameter(String key, Object value);

    /**
     * 获取分页数据
     *
     * @param start 起始位置
     * @param limit 数据条数
     * @return Criteria
     */
    Criteria limit(int start, int limit);

    /**
     * 辅助方法 动态设置是否打印日志
     *
     * @param withLog 是否打印日志
     * @return Criteria
     */
    Criteria withLog(boolean withLog);

    /**
     * 指定要查询的列，如果不指定则查询所有列
     *
     * @param fieldNames 需要查询字段对应持久化类中的属性
     * @return Criteria
     */
    Criteria specifyField(String... fieldNames);
}
