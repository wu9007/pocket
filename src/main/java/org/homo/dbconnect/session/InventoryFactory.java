package org.homo.dbconnect.session;

import org.homo.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/31
 */
@Component
public class InventoryFactory {
    private DatabaseConfig config;
    private static final Map<String, InventoryManager> INVENTORY_MANAGER_POOL = new ConcurrentHashMap<>(5);

    @Autowired
    private InventoryFactory(DatabaseConfig config, List<InventoryManager> inventoryManagerList) {
        this.config = config;
        inventoryManagerList.forEach(inventoryManager -> {
            INVENTORY_MANAGER_POOL.put(inventoryManager.getDbName(), inventoryManager);
        });
    }

    public InventoryManager getManager() {
        // TODO: 根据不同的数据源返回不同的内存管理员
        return INVENTORY_MANAGER_POOL.get(config.getName());
    }
}
