package org.hv.pocket.uuid;

import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.Session;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/2
 */
@Component
public class IncrementStrGenerator extends AbstractUuidGenerator {
    private final static UuidGenerator INSTANCE = new IncrementStrGenerator();
    private final static String UNDERLINE_DIVIDER = "_";

    public static UuidGenerator getInstance() {
        return INSTANCE;
    }

    private IncrementStrGenerator() {
    }

    @Override
    public void setGeneratorId() {
        this.generationType = GenerationType.STR_INCREMENT;
    }

    @Override
    public synchronized String getIdentify(Class clazz, Session session) throws SQLException {
        int tableId = MapperFactory.getTableId(clazz.getName());
        String mapKey = this.serverId + UNDERLINE_DIVIDER + tableId;
        String leaderNum = "" + serverId + tableId;
        Long tailNumber = POOL.get(mapKey);
        if (tailNumber != null) {
            tailNumber++;
        } else {
            long maxUuid = session.getMaxUuid(serverId, clazz);
            if (maxUuid == 0 || !String.valueOf(maxUuid).startsWith(leaderNum)) {
                tailNumber = 0L;
            } else {
                tailNumber = Long.parseLong(String.valueOf(maxUuid).substring(leaderNum.length())) + 1;
            }
        }
        POOL.put(mapKey, tailNumber);
        return "" + leaderNum + tailNumber;
    }
}
