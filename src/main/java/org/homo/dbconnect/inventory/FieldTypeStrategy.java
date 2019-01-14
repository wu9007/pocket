package org.homo.dbconnect.inventory;

import org.homo.dbconnect.annotation.Column;
import org.homo.dbconnect.annotation.ManyToOne;

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
public class FieldTypeStrategy {
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
        STRATEGY_POOL.put(Long.class.getName(), (resultSet, columnName) -> {
            try {
                return resultSet.getLong(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private FieldTypeStrategy() {
    }

    public static FieldTypeStrategy getInstance() {
        return strategy;
    }

    public Object getColumnValue(Field field, ResultSet resultSet) {
        Column column = field.getAnnotation(Column.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        String columnName;
        if (column != null) {
            columnName = column.name();
        } else if (manyToOne != null) {
            columnName = manyToOne.name();
        } else {
            throw new NullPointerException("未找到注解");
        }
        return STRATEGY_POOL.get(field.getType().getName()).apply(resultSet, columnName);
    }
}
