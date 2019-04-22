package org.hunter.pocket.model;

import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.constant.AnnotationType;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wujianchuan
 */
public class MapperFactory {

    private static AtomicBoolean completed = new AtomicBoolean(false);
    private static final Map<String, EntityMapper> ENTITY_MAPPER_POOL = new ConcurrentHashMap<>();

    public static String getTableName(String classFullName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getTableName();
    }

    public static AnnotationType getAnnotationType(String classFullName, String fieldName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getFieldMapper().get(fieldName).getAnnotationType();
    }

    public static Annotation getAnnotation(String classFullName, String fieldName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getFieldMapper().get(fieldName).getAnnotation();
    }

    public static String getRepositoryColumnName(String className, String fieldName) {
        return ENTITY_MAPPER_POOL.get(className).getRepositoryColumnMapper().get(fieldName);
    }

    public static List<String> getJoinSqlList(String classFullName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getJoinSqlList();
    }

    public static Map<String, String> getViewColumnMapper(String classFullName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getViewColumnMapper();
    }

    public static void init(ApplicationContext context) {
        if (!completed.get()) {
            Map<String, Object> beans = context.getBeansWithAnnotation(Entity.class);
            beans.forEach((name, bean) -> {
                Class clazz = bean.getClass();
                ENTITY_MAPPER_POOL.put(clazz.getName(), EntityMapper.newInstance(clazz));
            });

            completed.compareAndSet(false, true);
        }
    }
}
