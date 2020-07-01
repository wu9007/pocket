package org.hv.pocket.identify;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/2/14
 */
public class IdentifyGeneratorFactory {
    private static final Map<String, IdentifyGenerator> GENERATOR_POOL = new ConcurrentHashMap<>(6);
    private static final IdentifyGeneratorFactory OUR_INSTANCE = new IdentifyGeneratorFactory();

    public static IdentifyGeneratorFactory getInstance() {
        return OUR_INSTANCE;
    }

    private IdentifyGeneratorFactory() {
    }

    public IdentifyGenerator getIdentifyGenerator(String generationType) {
        return GENERATOR_POOL.get(generationType);
    }

    public void registerGenerator(IdentifyGenerator identifyGenerator) {
        synchronized (this) {
            if (GENERATOR_POOL.containsKey(identifyGenerator.getGenerationType())) {
                throw new IllegalArgumentException("This logo already exists. Please use another one.");
            } else {
                GENERATOR_POOL.put(identifyGenerator.getGenerationType(), identifyGenerator);
            }
        }
    }
}
