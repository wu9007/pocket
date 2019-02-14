package org.hunter.pocket.lunch;

import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.config.ServerConfig;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.session.SessionFactory;
import org.hunter.pocket.utils.CacheUtils;
import org.hunter.pocket.uuid.UuidGenerator;
import org.hunter.pocket.uuid.UuidGeneratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wujianchuan 2019/1/12
 */
@Component
@Order(value = 0)
public class DatabaseLauncher implements CommandLineRunner {
    private final
    DatabaseConfig databaseConfig;

    private final
    ServerConfig serverConfig;

    private final
    List<UuidGenerator> uuidGeneratorList;

    private final
    CacheUtils cacheUtils;

    @Autowired
    public DatabaseLauncher(DatabaseConfig databaseConfig, CacheUtils cacheUtils, ServerConfig serverConfig, List<UuidGenerator> uuidGeneratorList) {
        this.databaseConfig = databaseConfig;
        this.cacheUtils = cacheUtils;
        this.serverConfig = serverConfig;
        this.uuidGeneratorList = uuidGeneratorList;
    }

    @Override
    public void run(String... args) {
        this.initConnectionManager();
        this.initSessionFactory();
        this.initUuidGenerator();
    }

    private void initConnectionManager() {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        connectionManager.register(databaseConfig);
    }

    private void initSessionFactory() {
        SessionFactory.register(databaseConfig, cacheUtils);
    }

    private void initUuidGenerator() {
        UuidGeneratorFactory uuidGeneratorFactory = UuidGeneratorFactory.getInstance();
        Integer serverId = serverConfig.getServerId();
        this.uuidGeneratorList.forEach(uuidGenerator -> {
            uuidGenerator.setServerId(serverId);
            uuidGenerator.setGeneratorId();
            uuidGeneratorFactory.registerGenerator(uuidGenerator);
        });
    }
}
