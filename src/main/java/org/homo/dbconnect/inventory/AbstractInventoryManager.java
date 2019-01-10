package org.homo.dbconnect.inventory;

import org.homo.core.annotation.ManyToOne;
import org.homo.core.annotation.OneToMany;
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
    static final Predicate<Field> FINE_MAPPING_FILTER = field -> !SERIAL_VERSION_UID.equals(field.getName()) && (field.getAnnotation(Column.class) != null || field.getAnnotation(ManyToOne.class) != null);
    static final Predicate<Field> FIND_CHILDREN = field -> !SERIAL_VERSION_UID.equals(field.getName()) && field.getAnnotation(OneToMany.class) != null;
    static final Predicate<Field> FIND_PARENT = field -> !SERIAL_VERSION_UID.equals(field.getName()) && field.getAnnotation(ManyToOne.class) != null;

    /**
     * 获取表名
     *
     * @param clazz 类类型
     * @return 表名
     */
    String getTableName(Class clazz) {
        Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
        return annotation.table();
    }

    /**
     * 获取需要持久化的属性
     *
     * @param clazz 实体类
     * @return 需持久化的属性
     */
    Field[] getMappingField(Class clazz) {
        Field[] superFields = Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(FINE_MAPPING_FILTER).toArray(Field[]::new);
        Field[] fields = Arrays.stream(clazz.getDeclaredFields()).filter(FINE_MAPPING_FILTER).toArray(Field[]::new);
        int superLength = superFields.length;
        int implLength = fields.length;
        superFields = Arrays.copyOf(superFields, superLength + implLength);
        System.arraycopy(fields, 0, superFields, superLength, implLength);
        return superFields;
    }

    /**
     * 比较对象获取更新的字段
     *
     * @param modern 新对象
     * @param older  老对象
     * @return 需要更新的属性
     */
    Field[] dirtyFieldFilter(BaseEntity modern, BaseEntity older) {
        Field[] fields = this.getMappingField(modern.getClass());
        return Arrays.stream(fields)
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

    /**
     * 拼接列名
     *
     * @param fields 属性
     * @return 以逗号隔开的列名
     */
    String getColumnNames(Field[] fields) {
        StringBuilder sql = new StringBuilder();
        for (int index = 0; index < fields.length; index++) {
            Field field = fields[index];
            Column column = field.getAnnotation(Column.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            String columnName;
            if (column != null) {
                columnName = column.name();
            } else if (manyToOne != null) {
                columnName = manyToOne.name();
            } else {
                throw new NullPointerException("找不到注解");
            }
            sql.append(columnName);
            if (index < fields.length - 1) {
                sql.append(", ");
            }
        }
        return sql.toString();
    }

    /**
     * 拼接占位符
     *
     * @param fields 属性
     * @return 以逗号隔开的列名
     */
    String getColumnPlaceholder(Field[] fields) {
        StringBuilder sql = new StringBuilder();
        for (int index = 0; index < fields.length; index++) {
            sql.append("?");
            if (index < fields.length - 1) {
                sql.append(", ");
            }
        }
        return sql.toString();
    }
}
