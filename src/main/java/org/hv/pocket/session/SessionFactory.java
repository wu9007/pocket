package org.hv.pocket.session;

import org.hv.pocket.config.DatabaseConfig;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.constant.DatasourceDriverTypes;
import org.hv.pocket.exception.SessionException;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/31
 */
public class SessionFactory {
    private static final Map<String, DatabaseNodeConfig> NODE_POOL = new ConcurrentHashMap<>(5);
    private static final Map<String, CacheHolder> CACHE_POOL = new ConcurrentHashMap<>(5);

    private SessionFactory() {
    }

    public static void register(DatabaseConfig databaseConfig) {
        databaseConfig.getNode().forEach(databaseNodeConfig -> {
            String driverName = databaseNodeConfig.getDriverName();
            if (DatasourceDriverTypes.MYSQL_DRIVER.equals(driverName)
                    || DatasourceDriverTypes.ORACLE_DRIVER.equals(driverName)
                    || DatasourceDriverTypes.ORACLE_DRIVER_OLD.equals(driverName)) {
                Arrays.stream(databaseNodeConfig.getSession().split(CommonSql.COMMA))
                        .forEach(sessionName -> {
                            if (!NODE_POOL.containsKey(sessionName)) {
                                NODE_POOL.put(sessionName, databaseNodeConfig);
                                Integer cacheSize = databaseNodeConfig.getCacheSize();
                                if (cacheSize == null) {
                                    cacheSize = 100;
                                }
                                CACHE_POOL.put(sessionName, new CacheHolder(cacheSize));
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
        if (NODE_POOL.size() == 0) {
            throw new SessionException("Please wait a moment");
        }
        DatabaseNodeConfig databaseNodeConfig = NODE_POOL.get(sessionName);
        if (databaseNodeConfig == null) {
            throw new SessionException(String.format("No session named <<%s>> was found", sessionName));
        } else {
            return new SessionImpl(databaseNodeConfig, sessionName);
        }
    }

    /**
     * 获取session对应的缓存空间
     *
     * @param sessionName session name
     * @return cache
     */
    public static CacheHolder getCache(String sessionName) {
        return CACHE_POOL.get(sessionName);
    }
}
