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

    public InventoryManager getManager() {
        // TODO: 根据不同的数据源返回不同的内存管理员
        return new MysqlInventoryManager(databaseManager);
    }
}
