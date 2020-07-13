package org.hv.pocket.query;

import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan 2019/1/3
 */
public interface SQLQuery {

    /**
     * 增删改
     *
     * @return 影响行数
     * @throws SQLException 语句异常
     */
    int execute() throws SQLException;

    /**
     * 单条查询
     *
     * @return 查询结果
     * @throws SQLException 语句异常
     */
    Object unique() throws SQLException;

    /**
     * 查询所有数据
     *
     * @return list
     * @throws SQLException 语句异常
     */
    <E> List<E> list() throws SQLException;

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

    /**
     * 添加列名映射
     *
     * @param columnNames column name array
     * @return sql query
     */
    SQLQuery mapperColumn(String... columnNames);
}
