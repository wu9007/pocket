package org.homo.dbconnect.session;

import org.homo.dbconnect.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * @author wujianchuan 2018/12/31
 */
@Component
public class InventoryFactory {
    private DatabaseManager databaseManager;

    @Autowired
    private InventoryFactory(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public InventoryManager getManager(String managerName) {
        System.out.println("InventoryManagerName:" + managerName);
        Connection connection = this.databaseManager.getConn();
        return new MysqlInventoryManager(connection);
    }
}
