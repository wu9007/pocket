package org.hv.pocket.identify;

import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.Session;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * @author wujianchuan 2019/1/2
 */
@Component
public class IncrementLongGenerator extends AbstractIdentifyGenerator {

    private static final UnaryOperator<BigInteger> INTEGER_UNARY_OPERATOR = (item) -> item.add(BigInteger.ONE);

    @Override
    public void setGeneratorId() {
        this.generationType = GenerationType.INCREMENT;
    }

    @Override
    public Serializable getIdentify(Class<? extends AbstractEntity> clazz, Session session) {
        String tableName = MapperFactory.getTableName(clazz.getName());
        AtomicReference<BigInteger> serialNumber = POOL.getOrDefault(tableName, new AtomicReference<>(BigInteger.ZERO));
        String localDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        BigInteger baseIdentify = new BigInteger(localDateStr).multiply(new BigInteger("1000000"));
        if (serialNumber.get().compareTo(BigInteger.ZERO) == 0) {
            synchronized (this) {
                serialNumber = POOL.getOrDefault(tableName, new AtomicReference<>(BigInteger.ZERO));
                if (serialNumber.get().compareTo(BigInteger.ZERO) == 0) {
                    Number maxIdentify = this.getMaxIdentify(session, clazz);
                    if (maxIdentify != null && (new BigInteger(maxIdentify.toString())).compareTo(baseIdentify) > 0) {
                        serialNumber.compareAndSet(BigInteger.ZERO, new BigInteger(maxIdentify.toString()));
                    } else {
                        serialNumber.compareAndSet(BigInteger.ZERO, baseIdentify);
                    }
                    serialNumber.updateAndGet(INTEGER_UNARY_OPERATOR);
                    POOL.put(tableName, serialNumber);
                    return serialNumber;
                } else {
                    return serialNumber.updateAndGet(INTEGER_UNARY_OPERATOR);
                }
            }
        } else {
            if (serialNumber.get().compareTo(baseIdentify) < 0) {
                synchronized (this) {
                    serialNumber.set(baseIdentify);
                }
            }
            return serialNumber.updateAndGet(INTEGER_UNARY_OPERATOR);
        }
    }

    private Number getMaxIdentify(Session session, Class<? extends AbstractEntity> clazz) {
        String identifyFieldName = MapperFactory.getIdentifyFieldName(clazz.getName());
        Criteria criteria = session.createCriteria(clazz);
        return (Number) criteria.max(identifyFieldName);
    }
}
