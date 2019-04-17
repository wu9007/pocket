package org.hunter.pocket.query;

import java.util.List;

/**
 * @author wujianchuan 2019/1/3
 */
public interface SQLQuery {

    /**
     * 单条查询
     *
     * @return 查询结果
     */
    Object unique();

    /**
     * 查询所有数据
     *
     * @return list
     */
    List list();

    /**
     * 分页
     *
     * @param start 其实
     * @param limit 条数
     * @return SQLQuery
     */
    SQLQuery limit(int start, int limit);

    /**
     * 参数赋值
     *
     * @param key   键
     * @param value 值
     * @return SQLQuery
     */
    SQLQuery setParameter(String key, Object value);
}
