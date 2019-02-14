package org.hunter.pocket.uuid;

import org.hunter.pocket.session.Session;
import org.hunter.pocket.utils.ReflectUtils;
import org.springframework.stereotype.Component;

/**
 * @author wujianchuan 2019/1/2
 */
@Component
public class IncrementGenerator extends AbstractUuidGenerator {
    private final static UuidGenerator INSTANCE = new IncrementGenerator();

    public static UuidGenerator getInstance() {
        return INSTANCE;
    }

    private IncrementGenerator() {
    }

    @Override
    public void setGeneratorId() {
        this.generatorId = "increment";
    }

    @Override
    public synchronized long getUuid(Class clazz, Session session) throws Exception {
        int tableId = ReflectUtils.getInstance().getEntityAnnotation(clazz).tableId();
        String mapKey = this.serverId + "_" + tableId;
        String leaderNum = "" + serverId + tableId;
        Long tailNumber = POOL.get(mapKey);
        if (tailNumber != null) {
            tailNumber++;
        } else {
            long maxUuid = session.getMaxUuid(serverId, clazz);
            if (maxUuid == 0) {
                tailNumber = 0L;
            } else {
                tailNumber = Long.parseLong(String.valueOf(maxUuid).substring(leaderNum.length()));
            }
        }
        POOL.put(mapKey, tailNumber++);
        return Long.valueOf("" + leaderNum + tailNumber);
    }
}
