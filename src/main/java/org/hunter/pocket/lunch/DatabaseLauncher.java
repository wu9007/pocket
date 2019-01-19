package org.hunter.pocket.lunch;

import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.session.SessionFactory;
import org.hunter.pocket.utils.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author wujianchuan 2019/1/12
 */
@Component
@Order(value = 0)
public class DatabaseLauncher implements CommandLineRunner {
    private final
    DatabaseConfig databaseConfig;

    private final
    CacheUtils cacheUtils;

    @Autowired
    public DatabaseLauncher(DatabaseConfig databaseConfig, CacheUtils cacheUtils) {
        this.databaseConfig = databaseConfig;
        this.cacheUtils = cacheUtils;
    }

    @Override
    public void run(String... args) {
        ConnectionManager connectionFactory = ConnectionManager.getInstance();
        connectionFactory.register(databaseConfig);
        SessionFactory.register(databaseConfig, cacheUtils);
    }
}
