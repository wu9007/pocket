package org.hv.pocket.identify;

import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.Session;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wujianchuan 2019/1/2
 */
@Component
public class IncrementLongGenerator extends AbstractIdentifyGenerator {

    @Override
    public void setGeneratorId() {
        this.generationType = GenerationType.INCREMENT;
    }

    @Override
    public Serializable getIdentify(Class<? extends AbstractEntity> clazz, Session session) {
        String tableName = MapperFactory.getTableName(clazz.getName());
        AtomicLong serialNumber = POOL.getOrDefault(tableName, new AtomicLong(0L));
        String localDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long baseIdentify = Long.parseLong(localDateStr) * 1000000;
        if (serialNumber.get() == 0) {
            synchronized (this) {
                serialNumber = POOL.getOrDefault(tableName, new AtomicLong(0L));
                if (serialNumber.get() == 0) {
                    Number maxIdentify = this.getMaxIdentify(session, clazz);
                    if (maxIdentify != null && maxIdentify.longValue() > baseIdentify) {
                        serialNumber.addAndGet(maxIdentify.longValue());
                    } else {
                        serialNumber.addAndGet(baseIdentify);
                    }
                }
                serialNumber.incrementAndGet();
                POOL.put(tableName, serialNumber);
                return serialNumber.get();
            }
        } else {
            if (serialNumber.get() < baseIdentify) {
                serialNumber = new AtomicLong(baseIdentify);
            }
            return serialNumber.incrementAndGet();
        }
    }

    private Number getMaxIdentify(Session session, Class<? extends AbstractEntity> clazz) {
        String identifyFieldName = MapperFactory.getIdentifyFieldName(clazz.getName());
        Criteria criteria = session.createCriteria(clazz);
        return (Number) criteria.max(identifyFieldName);
    }
}
