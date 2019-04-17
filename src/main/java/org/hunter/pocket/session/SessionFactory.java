package org.hunter.pocket.session;

import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.config.DatabaseNodeConfig;
import org.hunter.pocket.constant.DatasourceDriverTypes;
import org.hunter.pocket.cache.BaseCacheUtils;
import org.hunter.pocket.exception.SessionException;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/31
 */
public class SessionFactory {
    private static final Map<String, DatabaseNodeConfig> NODE_POOL = new ConcurrentHashMap<>(5);
    private static BaseCacheUtils baseCacheUtils;

    private SessionFactory() {
    }

    public static void register(DatabaseConfig databaseConfig, BaseCacheUtils cacheUtils) {
        SessionFactory.baseCacheUtils = cacheUtils;
        databaseConfig.getNode().forEach(databaseNodeConfig -> {
            if (DatasourceDriverTypes.MYSQL_DRIVER.equals(databaseNodeConfig.getDriverName()) || DatasourceDriverTypes.ORACLE_DRIVER.equals(databaseNodeConfig.getDriverName())) {
                Arrays.stream(databaseNodeConfig.getSession().split(","))
                        .forEach(sessionName -> {
                            if (!NODE_POOL.containsKey(sessionName)) {
                                NODE_POOL.put(sessionName, databaseNodeConfig);
                            } else {
                                throw new SessionException("Session name duplicate.");
                            }
                        });
            } else {
                throw new SessionException("I'm sorry about that I don't support this database now.");
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
        return new SessionImpl(NODE_POOL.get(sessionName), sessionName, SessionFactory.baseCacheUtils);
    }
}
