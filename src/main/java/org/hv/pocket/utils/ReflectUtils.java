package org.hv.pocket.utils;

import org.hv.pocket.model.MapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author wujianchuan 2019/1/10
 */
public class ReflectUtils {
    private static final ReflectUtils OUR_INSTANCE = new ReflectUtils();
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtils.class);

    public static ReflectUtils getInstance() {
        return OUR_INSTANCE;
    }

    private ReflectUtils() {
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
                        if (modernValue == null && olderValue == null) {
                            return false;
                        }
                        if (modernValue == null) {
                            return true;
                        }
                        if (olderValue instanceof Number) {
                            return ((Comparable) modernValue).compareTo(olderValue) != 0;
                        }
                        return !modernValue.equals(olderValue);
                    } catch (IllegalAccessException e) {
                        LOGGER.warn(e.getMessage());
                        return false;
                    }
                })
                .toArray(Field[]::new);
    }
}
