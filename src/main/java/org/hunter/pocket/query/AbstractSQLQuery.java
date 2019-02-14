package org.hunter.pocket.query;

import java.sql.Connection;

/**
 * @author wujianchuan 2019/1/3
 */
abstract class AbstractSQLQuery {
    Connection connection;
    String sql;

    AbstractSQLQuery(String sql, Connection connection) {
        this.sql = sql;
        this.connection = connection;
    }

}
