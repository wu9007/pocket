package org.hv.pocket.flib;

import org.hv.pocket.function.PocketBiFunction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan
 */
final public class ResultSetFunctionLib {

    private static final Map<String, PocketBiFunction<ResultSet, String, Object>> RESULT_STRATEGY_POOL = new ConcurrentHashMap<>(20);

    static {
        RESULT_STRATEGY_POOL.put((int.class.getName()), ResultSet::getInt);
        RESULT_STRATEGY_POOL.put((byte.class.getName()), ResultSet::getByte);
        RESULT_STRATEGY_POOL.put((short.class.getName()), ResultSet::getShort);
        RESULT_STRATEGY_POOL.put((float.class.getName()), ResultSet::getFloat);
        RESULT_STRATEGY_POOL.put((double.class.getName()), ResultSet::getDouble);
        RESULT_STRATEGY_POOL.put((long.class.getName()), ResultSet::getLong);
        RESULT_STRATEGY_POOL.put((boolean.class.getName()), ResultSet::getBoolean);
        RESULT_STRATEGY_POOL.put(byte[].class.getName(), ResultSet::getBytes);
        RESULT_STRATEGY_POOL.put((Short.class.getName()), ResultSet::getShort);
        RESULT_STRATEGY_POOL.put((Byte.class.getName()), ResultSet::getByte);
        RESULT_STRATEGY_POOL.put(Byte[].class.getName(), ResultSet::getBytes);
        RESULT_STRATEGY_POOL.put(Integer.class.getName(), ResultSet::getInt);
        RESULT_STRATEGY_POOL.put(Long.class.getName(), ResultSet::getLong);
        RESULT_STRATEGY_POOL.put(Double.class.getName(), ResultSet::getDouble);
        RESULT_STRATEGY_POOL.put(String.class.getName(), ResultSet::getString);
        RESULT_STRATEGY_POOL.put(Serializable.class.getName(), ResultSet::getObject);
        RESULT_STRATEGY_POOL.put(BigDecimal.class.getName(), ResultSet::getBigDecimal);
        RESULT_STRATEGY_POOL.put(Date.class.getName(), (resultSet, columnName) -> {
            java.sql.Date sqlDate = resultSet.getDate(columnName);
            Date date = null;
            if (sqlDate != null) {
                date = new Date(sqlDate.getTime());
            }
            return date;
        });
        RESULT_STRATEGY_POOL.put(LocalDate.class.getName(), (resultSet, columnName) -> {
            java.sql.Date date = resultSet.getDate(columnName);
            return date != null ? date.toLocalDate() : null;
        });
        RESULT_STRATEGY_POOL.put(LocalDateTime.class.getName(), (resultSet, columnName) -> {
            Timestamp timestamp = resultSet.getTimestamp(columnName);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        });
        RESULT_STRATEGY_POOL.put(Boolean.class.getName(), (resultSet, columnName) -> {
            Object object = resultSet.getObject(columnName);
            if (object == null) {
                return null;
            } else if (object instanceof Boolean) {
                return object;
            } else {
                return Integer.parseInt(String.valueOf(object)) != 0;
            }
        });
    }

    static PocketBiFunction<ResultSet, String, Object> get(String fieldClassName) {
        return RESULT_STRATEGY_POOL.get(fieldClassName);
    }
}
