package org.homo.dbconnect.inventory;

import org.homo.core.annotation.Column;
import org.homo.core.annotation.Entity;
import org.homo.core.model.BaseEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * @author wujianchuan 2019/1/9
 */
abstract class AbstractInventoryManager implements InventoryManager {

    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    static final String MYSQL_DB_NAME = "com.mysql.cj.jdbc.Driver";
    static final Predicate<Field> NO_MAPPING_FILTER = field -> !SERIAL_VERSION_UID.equals(field.getName()) && field.getAnnotation(Column.class) != null;

    String getTableName(Class clazz) {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        return annotation.table();
    }

    Field[] dirtyFieldFilter(BaseEntity modern, BaseEntity older) {
        Field[] fields = modern.getClass().getDeclaredFields();
        return Arrays.stream(fields)
                .filter(NO_MAPPING_FILTER)
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        Object modernValue = field.get(modern);
                        Object olderValue = field.get(older);
                        return modernValue == null && olderValue != null || olderValue == null && modernValue != null || modernValue != null && !modernValue.equals(olderValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .toArray(Field[]::new);
    }
}
