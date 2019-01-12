package org.homo.dbconnect.inventory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/31
 */
public class SessionFactory {
    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>(5);

    private SessionFactory(List<Session> inventoryManagerList) {
        inventoryManagerList.forEach(inventoryManager -> {
            SESSION_POOL.put(inventoryManager.getDbName(), inventoryManager);
        });
    }

    public static Session getSession(String databaseName, String sessionName) {
        // TODO: 根据不同的数据源和缓存名称返回不同的缓存对象
        return SESSION_POOL.get(databaseName + sessionName);
    }
}
