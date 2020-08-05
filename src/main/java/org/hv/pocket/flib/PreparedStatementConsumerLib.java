package org.hv.pocket.flib;

import org.hv.pocket.function.PocketConsumer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan
 */
final public class PreparedStatementConsumerLib {
    static final Map<String, PocketConsumer<PreparedSupplierValue>> PREPARED_STRATEGY_POOL = new ConcurrentHashMap<>(20);

    static {
        PREPARED_STRATEGY_POOL.put(boolean.class.getName(), (value) -> value.getPreparedStatement().setBoolean(value.getIndex(), (Boolean) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(short.class.getName(), (value) -> value.getPreparedStatement().setShort(value.getIndex(), (Short) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(byte.class.getName(), (value) -> value.getPreparedStatement().setByte(value.getIndex(), (Byte) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(int.class.getName(), (value) -> value.getPreparedStatement().setInt(value.getIndex(), (Integer) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(long.class.getName(), (value) -> value.getPreparedStatement().setLong(value.getIndex(), (Long) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(float.class.getName(), (value) -> value.getPreparedStatement().setFloat(value.getIndex(), (Float) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(double.class.getName(), (value) -> value.getPreparedStatement().setDouble(value.getIndex(), (Double) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(byte.class.getName(), (value) -> value.getPreparedStatement().setByte(value.getIndex(), (byte) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Byte.class.getName(), (value) -> value.getPreparedStatement().setByte(value.getIndex(), (Byte) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(byte[].class.getName(), (value) -> value.getPreparedStatement().setBytes(value.getIndex(), (byte[]) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Byte[].class.getName(), (value) -> value.getPreparedStatement().setObject(value.getIndex(), value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(short.class.getName(), (value) -> value.getPreparedStatement().setShort(value.getIndex(), (short) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Short.class.getName(), (value) -> value.getPreparedStatement().setShort(value.getIndex(), (Short) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Boolean.class.getName(), (value) -> value.getPreparedStatement().setBoolean(value.getIndex(), (Boolean) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Integer.class.getName(), (value) -> value.getPreparedStatement().setInt(value.getIndex(), (Integer) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(BigDecimal.class.getName(), (value) -> value.getPreparedStatement().setBigDecimal(value.getIndex(), (BigDecimal) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(String.class.getName(), (value) -> value.getPreparedStatement().setString(value.getIndex(), (String) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Serializable.class.getName(), (value) -> value.getPreparedStatement().setObject(value.getIndex(), value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Long.class.getName(), (value) -> value.getPreparedStatement().setLong(value.getIndex(), (Long) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Double.class.getName(), (value) -> value.getPreparedStatement().setDouble(value.getIndex(), (Double) value.getSqlBean().getTarget()));
        PREPARED_STRATEGY_POOL.put(Date.class.getName(), (value) -> value.getPreparedStatement().setTimestamp(value.getIndex(), new Timestamp(((Date) value.getSqlBean().getTarget()).getTime())));
        PREPARED_STRATEGY_POOL.put(LocalDate.class.getName(), (value) -> value.getPreparedStatement().setDate(value.getIndex(), java.sql.Date.valueOf((LocalDate) value.getSqlBean().getTarget())));
        PREPARED_STRATEGY_POOL.put(LocalDateTime.class.getName(), (value) -> value.getPreparedStatement().setTimestamp(value.getIndex(), Timestamp.valueOf((LocalDateTime) value.getSqlBean().getTarget())));
    }

    static PocketConsumer<PreparedSupplierValue> get(String fieldClassName) {
        return PREPARED_STRATEGY_POOL.get(fieldClassName);
    }
}
