package org.hv.pocket.session;

import org.hv.pocket.config.DatabaseConfig;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.constant.DatasourceDriverTypes;
import org.hv.pocket.exception.CriteriaException;
import org.hv.pocket.exception.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wujianchuan 2018/12/31
 */
public class SessionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactory.class);

    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private static final AtomicInteger RETRY = new AtomicInteger(0);
    private static final int MAX_RETRY_TIMES = 5;
    private static final Map<String/* session name */, DatabaseNodeConfig/* node config */> NODE_POOL = new ConcurrentHashMap<>(5);
    private static final Map<String, CacheHolder> CACHE_POOL = new ConcurrentHashMap<>(5);
    private static final List<String> NODES = new ArrayList<>();

    private SessionFactory() {
    }

    /**
     * Register the database configuration corresponding to each Session name
     * and create a corresponding cache
     *
     * @param databaseConfig database config
     */
    public static void register(DatabaseConfig databaseConfig) {
        for (DatabaseNodeConfig databaseNodeConfig : databaseConfig.getNode()) {
            String nodeName = databaseNodeConfig.getNodeName();
            if (NODES.contains(nodeName)) {
                throw new SessionException("Node name duplicate.");
            }
            NODES.add(nodeName);
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
        }
    }

    /**
     * Gets a database session object
     *
     * @param sessionName session name
     * @return session
     */
    public static Session getSession(String sessionName) {
        if (NODE_POOL.size() == 0) {
            if (RETRY.getAndIncrement() > MAX_RETRY_TIMES) {
                throw new CriteriaException("The maximum number of attempts to get a session has been reached.");
            }
            LOCK.lock();
            try {
                if (NODE_POOL.size() == 0) {
                    Thread.sleep(100);
                }
                return getSession(sessionName);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            } finally {
                LOCK.unlock();
            }
        }
        DatabaseNodeConfig databaseNodeConfig = NODE_POOL.get(sessionName);
        if (databaseNodeConfig == null) {
            throw new SessionException(String.format("No session named <<%s>> was found", sessionName));
        } else {
            return new SessionImpl(databaseNodeConfig, sessionName);
        }
    }

    /**
     * 获Gets the cached object for a database session
     *
     * @param sessionName session name
     * @return cache
     */
    public static CacheHolder getCache(String sessionName) {
        return CACHE_POOL.get(sessionName);
    }
}
