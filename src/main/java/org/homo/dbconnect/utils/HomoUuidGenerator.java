package org.homo.dbconnect.utils;

import org.homo.dbconnect.session.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wujianchuan 2019/1/2
 */
public class HomoUuidGenerator implements UuidGenerator {
    private final static Map<String, AtomicLong> POOL = new ConcurrentHashMap<>(60);
    private final static UuidGenerator INSTANCE = new HomoUuidGenerator();

    public static UuidGenerator getInstance() {
        return INSTANCE;
    }

    private HomoUuidGenerator() {
    }

    @Override
    public long getUuid(Class clazz, Session session) throws Exception {
        AtomicLong uuid = POOL.get(clazz.getName());
        if (uuid != null) {
            return uuid.addAndGet(1);
        } else {
            synchronized (this) {
                uuid = new AtomicLong(session.getMaxUuid(clazz));
                POOL.put(clazz.getName(), uuid);
                return POOL.get(clazz.getName()).addAndGet(1);
            }
        }
    }
}
