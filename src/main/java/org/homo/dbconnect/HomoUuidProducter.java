package org.homo.dbconnect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/1/2
 */
public class HomoUuidProducter implements UuidProducer {
    private final static UuidProducer INSTANCE = new HomoUuidProducter();
    private final static Map<String, String> POOL = new ConcurrentHashMap<>(60);

    private HomoUuidProducter() {
    }

    static UuidProducer getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized String getUuid(Class clazz) {
        String uuid = POOL.get(clazz.getName());
        if (uuid != null) {
            // TODO 数据库查询
            uuid = String.valueOf(Integer.parseInt(uuid) + 1);
            return uuid;
        } else {
            POOL.put(clazz.getName(), "1000");
            return POOL.get(clazz.getName());
        }
    }
}
