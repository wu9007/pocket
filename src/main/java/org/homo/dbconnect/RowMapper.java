package org.homo.dbconnect;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wujianchuan
 */
public interface RowMapper<T> {
    /**
     * 返回对象实体化
     *
     * @param resultSet ResultSet集合
     * @return 对象
     * @throws SQLException sql异常
     */
    T getRow(ResultSet resultSet) throws SQLException;
}
