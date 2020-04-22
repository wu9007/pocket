package org.hv.pocket.uuid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/2/14
 */
public class UuidGeneratorFactory {
    private static final Map<GenerationType, UuidGenerator> GENERATOR_POOL = new ConcurrentHashMap<>(6);
    private static final UuidGeneratorFactory OUR_INSTANCE = new UuidGeneratorFactory();

    public static UuidGeneratorFactory getInstance() {
        return OUR_INSTANCE;
    }

    private UuidGeneratorFactory() {
    }

    public UuidGenerator getUuidGenerator(GenerationType generationType) {
        return GENERATOR_POOL.get(generationType);
    }

    public void registerGenerator(UuidGenerator uuidGenerator) {
        synchronized (this) {
            if (GENERATOR_POOL.containsKey(uuidGenerator.getGenerationType())) {
                throw new IllegalArgumentException("This logo already exists. Please use another one.");
            } else {
                GENERATOR_POOL.put(uuidGenerator.getGenerationType(), uuidGenerator);
            }
        }
    }
}
