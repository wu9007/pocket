package org.homo.pocket.session;

import org.homo.pocket.config.DatabaseConfig;
import org.homo.pocket.constant.DatasourceDriverTypes;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/31
 */
public class SessionFactory {
    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>(5);

    private SessionFactory() {
    }

    public static void register(DatabaseConfig databaseConfig) {
        databaseConfig.getNode().forEach(databaseNodeConfig -> {
            if (DatasourceDriverTypes.MYSQL_DRIVER.equals(databaseNodeConfig.getDriverName()) || DatasourceDriverTypes.ORACLE_DRIVER.equals(databaseNodeConfig.getDriverName())) {
                Arrays.stream(databaseNodeConfig.getSession().split(",")).forEach(name -> {
                    SESSION_POOL.put(name, new SessionImpl(databaseNodeConfig));
                });
            } else {
                throw new RuntimeException("I'm sorry about that I don't support this database now.");
            }
        });
    }

    public static Session getSession(String sessionName) {
        return SESSION_POOL.get(sessionName);
    }
}
