package org.hunter.pocket.utils;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.annotation.OneToMany;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.criteria.FieldMapper;
import org.hunter.pocket.model.MapperFactory;
import org.hunter.pocket.model.PocketEntity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author wujianchuan 2019/1/10
 */
public class ReflectUtils {
    private static final ReflectUtils OUR_INSTANCE = new ReflectUtils();
    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    private static final Predicate<Field> FIND_MAPPING_FILTER = field -> !SERIAL_VERSION_UID.equals(field.getName()) && (field.getAnnotation(Column.class) != null || field.getAnnotation(ManyToOne.class) != null || field.getAnnotation(Join.class) != null);
    public static final Predicate<Field> FIND_CHILDREN = field -> !SERIAL_VERSION_UID.equals(field.getName()) && field.getAnnotation(OneToMany.class) != null;

    private static final Function<Field, FieldMapper> MAP_FIELD_MAPPER = field -> {
        Column column = field.getAnnotation(Column.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        Join join = field.getAnnotation(Join.class);
        String columnName;
        if (column != null) {
            columnName = column.name();
        } else if (manyToOne != null) {
            columnName = manyToOne.columnName();
        } else {
            columnName = join.columnName();
        }
        return FieldMapper.newInstance(field.getName(), columnName, field);
    };

    public static ReflectUtils getInstance() {
        return OUR_INSTANCE;
    }

    private ReflectUtils() {
    }

    /**
     * 获取表名
     *
     * @param clazz 类类型
     * @return 表名
     */
    @Deprecated
    public Entity getEntityAnnotation(Class clazz) {
        return (Entity) clazz.getAnnotation(Entity.class);
    }

    /**
     * 获取数据标识
     *
     * @param obj 实体
     * @return 数据标识
     */
    public Serializable getUuidValue(Object obj) {
        Serializable value;
        Class clazz = obj.getClass();
        Field uuid;
        try {
            uuid = clazz.getSuperclass().getDeclaredField("uuid");
            uuid.setAccessible(true);
            value = (Serializable) uuid.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("no uuid fond.");
        }
        return value;
    }

    /**
     * 数据标识赋值
     *
     * @param obj   实体
     * @param value 数据标识
     * @return 实体
     */
    public Object setUuidValue(Object obj, Serializable value) {
        Class clazz = obj.getClass();
        Field uuid;
        try {
            uuid = clazz.getSuperclass().getDeclaredField("uuid");
            uuid.setAccessible(true);
            uuid.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("no uuid fond.");
        }
        return obj;
    }

    /**
     * 获取需要持久化的属性
     *
     * @param clazz 实体类
     * @return 需持久化的属性
     */
    @Deprecated
    private Field[] getMappingFields(Class clazz) {
        Field[] superFields = Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(FIND_MAPPING_FILTER).toArray(Field[]::new);
        Field[] fields = Arrays.stream(clazz.getDeclaredFields()).filter(FIND_MAPPING_FILTER).toArray(Field[]::new);
        return (Field[]) CommonUtils.combinedArray(superFields, fields);
    }

    /**
     * 获取需要持久化的属性
     *
     * @param clazz 实体类
     * @return 需持久化的属性
     */
    public Field[] getFields(Class clazz) {
        Field[] superFields = Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(field -> !SERIAL_VERSION_UID.equals(field.getName())).toArray(Field[]::new);
        Field[] fields = Arrays.stream(clazz.getDeclaredFields()).filter(field -> !SERIAL_VERSION_UID.equals(field.getName())).toArray(Field[]::new);
        return (Field[]) CommonUtils.combinedArray(superFields, fields);
    }

    /**
     * 获取属性映射数组
     *
     * @param clazz 类类型
     * @return 映射数组
     */
    @Deprecated
    public Map<String, FieldMapper> getFieldMapperMap(Class clazz) {
        Map<String, FieldMapper> fieldMapper = Arrays.stream(clazz.getDeclaredFields())
                .filter(FIND_MAPPING_FILTER)
                .map(MAP_FIELD_MAPPER)
                .collect(Collectors.toMap(FieldMapper::getFieldName, FieldMapper::new));
        Map<String, FieldMapper> superFieldMapper = Arrays
                .stream(clazz.getSuperclass().getDeclaredFields())
                .filter(FIND_MAPPING_FILTER)
                .map(MAP_FIELD_MAPPER)
                .collect(Collectors.toMap(FieldMapper::getFieldName, FieldMapper::new));
        superFieldMapper.putAll(fieldMapper);
        return superFieldMapper;
    }

    /**
     * 比较对象获取更新的字段
     *
     * @param modern 新对象
     * @param older  老对象
     * @return 需要更新的属性
     */
    public Field[] dirtyFieldFilter(Object modern, Object older) {
        Field[] fields = MapperFactory.getRepositoryFields(modern.getClass().getName());
        return Arrays.stream(fields)
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        Object modernValue = field.get(modern);
                        Object olderValue = field.get(older);
                        if (olderValue instanceof Number) {
                            return ((Comparable) modernValue).compareTo(olderValue) != 0;
                        }
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
    @Deprecated
    public List getColumnNames(Field[] fields) {
        List<String> columnNames = new LinkedList<>();
        for (Field field : fields) {
            String columnName;
            Column column = field.getAnnotation(Column.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            Join join = field.getAnnotation(Join.class);
            if (column != null) {
                columnName = column.name();
            } else if (manyToOne != null) {
                columnName = manyToOne.columnName();
            } else if (join != null) {
                columnName = join.columnName();
            } else {
                throw new NullPointerException("找不到注解");
            }
            columnNames.add(columnName);
        }
        return columnNames;
    }

    /**
     * 拼接列名
     *
     * @param entity 实体
     * @return 以逗号隔开的列名
     */
    @Deprecated
    public Field[] getValueNotNullFields(PocketEntity entity) {
        Field[] fields = this.getMappingFields(entity.getClass());
        List<Field> valueNotNullFields = new LinkedList<>();
        int count = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.get(entity) != null) {
                    valueNotNullFields.add(field);
                    count++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return valueNotNullFields.toArray(new Field[count]);
    }

    /**
     * 拼接占位符
     *
     * @param fields 属性
     * @return 以逗号隔开的列名
     */
    @Deprecated
    public String getColumnPlaceholder(Field[] fields) {
        StringBuilder sql = new StringBuilder();
        for (int index = 0; index < fields.length; index++) {
            sql.append(CommonSql.PLACEHOLDER);
            if (index < fields.length - 1) {
                sql.append(CommonSql.COMMA);
            }
        }
        return sql.toString();
    }
}
