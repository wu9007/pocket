package org.hv.pocket.lunch;

import org.hv.pocket.annotation.Entity;
import org.hv.pocket.config.DatabaseConfig;
import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.session.SessionFactory;
import org.hv.pocket.uuid.UuidGenerator;
import org.hv.pocket.uuid.UuidGeneratorFactory;
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
    List<UuidGenerator> uuidGeneratorList;

    private final
    List<AbstractEntity> entityList;

    @Autowired
    public DatabaseLauncher(DatabaseConfig databaseConfig, List<UuidGenerator> uuidGeneratorList, @Nullable List<AbstractEntity> entityList) {
        this.databaseConfig = databaseConfig;
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
                    throw new IllegalArgumentException("Table ID - " + entityAnnotation.tableId() + " repeated.");
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
        SessionFactory.register(databaseConfig);
    }

    private void initUuidGenerator() {
        UuidGeneratorFactory uuidGeneratorFactory = UuidGeneratorFactory.getInstance();
        Integer serverId = databaseConfig.getServerId();
        this.uuidGeneratorList.forEach(uuidGenerator -> {
            uuidGenerator.setServerId(serverId);
            uuidGenerator.setGeneratorId();
            uuidGeneratorFactory.registerGenerator(uuidGenerator);
        });
    }
}
