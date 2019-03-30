package org.hunter.pocket.query;

import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan 2019/1/3
 */
public interface SQLQuery {

    /**
     * 单条查询
     *
     * @return 查询结果
     * @throws SQLException e
     */
    Object unique() throws SQLException;

    /**
     * 查询所有数据
     *
     * @return list
     * @throws SQLException e
     */
    List list() throws SQLException;

    /**
     * 分页
     *
     * @param start 其实
     * @param limit 条数
     * @return SQLQuery
     */
    SQLQuery limit(int start, int limit);
}
