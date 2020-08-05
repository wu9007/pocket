package org.hv.pocket.session.actions;

import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.query.ProcessQuery;
import org.hv.pocket.query.SQLQuery;

/**
 * @author wujianchuan
 */
public interface BuildDictionary {

    /**
     * 获取SQL查询对象
     *
     * @return 查询对象
     */
    SQLQuery createSQLQuery();

    /**
     * 获取SQL查询对象
     *
     * @param sql 查询语句
     * @return 查询对象
     */
    SQLQuery createSQLQuery(String sql);

    /**
     * 获取SQL规范对象
     *
     * @param clazz 实体类类型
     * @return SQL规范对象
     */
    Criteria createCriteria(Class<? extends AbstractEntity> clazz);

    /**
     * 获取SQL查询对象
     *
     * @param sql   查询语句
     * @param clazz 返回类型
     * @return 查询对象
     */
    SQLQuery createSQLQuery(String sql, Class<?> clazz);

    /**
     * 获取ProcessSQL查询对象
     *
     * @param processSQL 存储过程SQL
     * @return 查询对象
     */
    <T extends AbstractEntity> ProcessQuery<T> createProcessQuery(String processSQL);
}
