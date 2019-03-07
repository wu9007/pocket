package org.hunter.pocket.session;

import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.DatasourceDriverTypes;
import org.hunter.pocket.utils.CacheUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/31
 */
public class SessionFactory {
    private static final Map<String, DatabaseNodeConfig> NODE_POOL = new ConcurrentHashMap<>(5);
    private static CacheUtils cacheUtils;

    private SessionFactory() {
    }

    public static void register(DatabaseConfig databaseConfig, CacheUtils cacheUtils) {
        SessionFactory.cacheUtils = cacheUtils;
        databaseConfig.getNode().forEach(databaseNodeConfig -> {
            if (DatasourceDriverTypes.MYSQL_DRIVER.equals(databaseNodeConfig.getDriverName()) || DatasourceDriverTypes.ORACLE_DRIVER.equals(databaseNodeConfig.getDriverName())) {
                Arrays.stream(databaseNodeConfig.getSession().split(","))
                        .forEach(sessionName -> {
                            if (!NODE_POOL.containsKey(sessionName)) {
                                NODE_POOL.put(sessionName, databaseNodeConfig);
                            } else {
                                throw new RuntimeException("Session name duplicate.");
                            }
                        });
            } else {
                throw new RuntimeException("I'm sorry about that I don't support this database now.");
            }
        });
    }

    /**
     * 新建一个session对象
     *
     * @param sessionName session name
     * @return session
     */
    public static Session getSession(String sessionName) {
        return new SessionImpl(NODE_POOL.get(sessionName), sessionName, SessionFactory.cacheUtils);
    }
}
