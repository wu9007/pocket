package org.hunter.pocket.uuid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/2/14
 */
public class UuidGeneratorFactory {
    private static final Map<String, UuidGenerator> GENERATOR_POOL = new ConcurrentHashMap<>(6);
    private static final UuidGeneratorFactory OUR_INSTANCE = new UuidGeneratorFactory();

    public static UuidGeneratorFactory getInstance() {
        return OUR_INSTANCE;
    }

    private UuidGeneratorFactory() {
    }

    public UuidGenerator getUuidGenerator(String generatorId) {
        return GENERATOR_POOL.get(generatorId);
    }

    public void registerGenerator(UuidGenerator uuidGenerator) {
        synchronized (this) {
            if (GENERATOR_POOL.containsKey(uuidGenerator.getGeneratorId())) {
                throw new IllegalArgumentException("This logo already exists. Please use another one.");
            } else {
                GENERATOR_POOL.put(uuidGenerator.getGeneratorId(), uuidGenerator);
            }
        }
    }
}
