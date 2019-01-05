package org.homo.dbconnect.session;

import org.homo.core.annotation.Column;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @author wujianchuan 2019/1/3
 */
class FieldTypeStrategy {
    private static FieldTypeStrategy strategy = new FieldTypeStrategy();
    private static final Map<String, BiFunction<ResultSet, String, Object>> STRATEGY_POOL = new ConcurrentHashMap<>(20);

    static {
        STRATEGY_POOL.put(String.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getString(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        STRATEGY_POOL.put(BigDecimal.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getBigDecimal(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private FieldTypeStrategy() {
    }

    static FieldTypeStrategy getInstance() {
        return strategy;
    }

    Object getColumnValue(Field field, ResultSet resultSet) {
        Column annotation = field.getAnnotation(Column.class);
        String columnName = annotation.name();
        return STRATEGY_POOL.get(field.getType().getName()).apply(resultSet, columnName);
    }
}
