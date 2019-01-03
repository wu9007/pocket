package org.homo.dbconnect.query;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/3
 */
public interface Query {

    /**
     * 单条查询
     *
     * @return 查询结果
     */
    abstract Object unique() throws SQLException;
}
