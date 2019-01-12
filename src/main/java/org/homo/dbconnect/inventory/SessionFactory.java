package org.homo.dbconnect.inventory;

import org.homo.dbconnect.config.AbstractDatabaseConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/31
 */
public class SessionFactory {
    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>(5);

    private SessionFactory() {
    }

    //TODO: 启动时导入多有Session对象
    public static void register(AbstractDatabaseConfig databaseConfig) {
        Session session = new SessionImpl(databaseConfig);
        SESSION_POOL.put(databaseConfig.getDatabaseName(), session);
    }

    public static Session getSession(String databaseName) {
        return SESSION_POOL.get(databaseName);
    }
}
