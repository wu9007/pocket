package org.homo.dbconnect.session;

import org.homo.dbconnect.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * @author wujianchuan 2018/12/31
 */
@Component
public class SessionFactory {
    private DatabaseManager databaseManager;

    @Autowired
    private SessionFactory(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Session getSession(String sessionName) {
        System.out.println("sessionName:" + sessionName);
        Connection connection = this.databaseManager.getConn();
        return new HomoSession(connection);
    }
}
