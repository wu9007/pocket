package org.hv.pocket.identify;

import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.Session;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/2
 */
@Component
public class IncrementStrGenerator extends AbstractIdentifyGenerator {
    private final static IdentifyGenerator INSTANCE = new IncrementStrGenerator();
    private final static String UNDERLINE_DIVIDER = "_";

    public static IdentifyGenerator getInstance() {
        return INSTANCE;
    }

    private IncrementStrGenerator() {
    }

    @Override
    public void setGeneratorId() {
        this.generationType = GenerationType.STR_INCREMENT;
    }

    @Override
    public synchronized String getIdentify(Class<? extends AbstractEntity> clazz, Session session) throws SQLException {
        int tableId = MapperFactory.getTableId(clazz.getName());
        String mapKey = this.serverId + UNDERLINE_DIVIDER + tableId;
        String leaderNum = "" + serverId + tableId;
        Long tailNumber = POOL.get(mapKey);
        if (tailNumber != null) {
            tailNumber++;
        } else {
            long maxIdentify = session.getMaxIdentify(serverId, clazz);
            if (maxIdentify == 0 || !String.valueOf(maxIdentify).startsWith(leaderNum)) {
                tailNumber = 0L;
            } else {
                tailNumber = Long.parseLong(String.valueOf(maxIdentify).substring(leaderNum.length())) + 1;
            }
        }
        POOL.put(mapKey, tailNumber);
        return "" + leaderNum + tailNumber;
    }
}
