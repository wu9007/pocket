package org.homo.dbconnect.connect;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/15
 */
interface ConnectionPool {

    void init();

    Connection getConnection();

    Connection newConnection();

    Connection getCurrentConnection();

    void releaseConn(Connection conn) throws SQLException;

    void destroy();

    boolean isActive();

    void checkPool();

    int getActiveNum();

    int getFreeNum();
}
