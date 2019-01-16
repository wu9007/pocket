package org.homo.dbconnect.session;

import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.constant.DatasourceDriverTypes;

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

    public static void register(AbstractDatabaseConfig databaseConfig) {
        if (DatasourceDriverTypes.MYSQL_DRIVER.equals(databaseConfig.getDriverName()) || DatasourceDriverTypes.ORACLE_DRIVER.equals(databaseConfig.getDriverName())) {
            Arrays.stream(databaseConfig.getSession().split(",")).forEach(name -> {
                SESSION_POOL.put(name, new SessionImpl(databaseConfig));
            });
        } else {
            throw new RuntimeException("I'm sorry about that I don't support this database now.");
        }
    }

    public static Session getSession(String node) {
        return SESSION_POOL.get(node);
    }
}
