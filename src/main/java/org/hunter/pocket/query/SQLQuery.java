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
     */
    List list() throws SQLException;
}
