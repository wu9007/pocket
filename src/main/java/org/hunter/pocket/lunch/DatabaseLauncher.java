package org.hunter.pocket.lunch;

import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.config.ServerConfig;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.model.BaseEntity;
import org.hunter.pocket.session.SessionFactory;
import org.hunter.pocket.cache.BaseCacheUtils;
import org.hunter.pocket.uuid.UuidGenerator;
import org.hunter.pocket.uuid.UuidGeneratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wujianchuan 2019/1/12
 */
@Component
@Order(value = -100)
public class DatabaseLauncher implements CommandLineRunner {
    private final
    DatabaseConfig databaseConfig;

    private final
    ServerConfig serverConfig;

    private final
    List<UuidGenerator> uuidGeneratorList;

    private final
    List<BaseEntity> entityList;

    private final
    BaseCacheUtils baseCacheUtils;

    @Autowired
    public DatabaseLauncher(DatabaseConfig databaseConfig, BaseCacheUtils baseCacheUtils, ServerConfig serverConfig, List<UuidGenerator> uuidGeneratorList, @Nullable List<BaseEntity> entityList) {
        this.databaseConfig = databaseConfig;
        this.baseCacheUtils = baseCacheUtils;
        this.serverConfig = serverConfig;
        this.uuidGeneratorList = uuidGeneratorList;
        this.entityList = entityList;
    }

    @Override
    public void run(String... args) {
        this.verifyEntity();
        this.initConnectionManager();
        this.initSessionFactory();
        this.initUuidGenerator();
    }

    private void verifyEntity() {
        Map<Integer, Boolean> counter = new HashMap<>(260);
        if (this.entityList != null) {
            for (Entity entityAnnotation : entityList.stream().map(entity -> entity.getClass().getAnnotation(Entity.class)).collect(Collectors.toList())) {
                if (counter.containsKey(entityAnnotation.tableId())) {
                    throw new RuntimeException("Table ID - " + entityAnnotation.tableId() + " repeated.");
                } else {
                    counter.put(entityAnnotation.tableId(), true);
                }
            }
        }
    }

    private void initConnectionManager() {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        connectionManager.register(databaseConfig);
    }

    private void initSessionFactory() {
        SessionFactory.register(databaseConfig, baseCacheUtils);
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
