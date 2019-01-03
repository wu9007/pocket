package org.homo.dbconnect.query;

import java.sql.Connection;

/**
 * @author wujianchuan 2019/1/3
 */
public abstract class AbstractQuery implements Query {
    Connection connection;
    String sql;

    AbstractQuery(String sql, Connection connection) {
        this.sql = sql;
        this.connection = connection;
    }

}
