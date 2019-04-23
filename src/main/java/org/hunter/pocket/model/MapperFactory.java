package org.hunter.pocket.model;

import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.constant.AnnotationType;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wujianchuan
 */
public class MapperFactory {

    private static final AtomicBoolean COMPLETED = new AtomicBoolean(false);
    private static final Map<String, EntityMapper> ENTITY_MAPPER_POOL = new ConcurrentHashMap<>();

    /**
     * 获取表ID
     *
     * @param classFullName class name
     * @return table id
     */
    public static int getTableId(String classFullName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getTableId();
    }

    /**
     * 获取表名
     *
     * @param classFullName class name
     * @return table name
     */
    public static String getTableName(String classFullName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getTableName();
    }

    /**
     * 获取主键生成策略
     *
     * @param classFullName class name
     * @return uuid generator
     */
    public static String getUuidGenerator(String classFullName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getUuidGenerator();
    }

    /**
     * 获取属性上的注解类型
     *
     * @param classFullName class name
     * @param fieldName     field name
     * @return annotation type
     */
    public static AnnotationType getAnnotationType(String classFullName, String fieldName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getFieldMapper().get(fieldName).getAnnotationType();
    }

    /**
     * 获取属性上的注解
     *
     * @param classFullName class name
     * @param fieldName     field name
     * @return annotation
     */
    public static Annotation getAnnotation(String classFullName, String fieldName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getFieldMapper().get(fieldName).getAnnotation();
    }

    /**
     * 获取需要持久化的属性
     *
     * @param className class name
     * @return need repository fields
     */
    public static Field[] getRepositoryFields(String className) {
        return ENTITY_MAPPER_POOL.get(className).getRepositoryFields();
    }

    /**
     * 获取徐持久化的列名
     *
     * @param className class name
     * @return column names
     */
    public static List<String> getRepositoryColumnNames(String className) {
        return ENTITY_MAPPER_POOL.get(className).getRepositoryColumnNames();
    }

    /**
     * 获取指定徐持久化的列名
     *
     * @param className class name
     * @param fieldName field name
     * @return column name
     */
    public static String getRepositoryColumnName(String className, String fieldName) {
        return ENTITY_MAPPER_POOL.get(className).getRepositoryColumnMapper().get(fieldName);
    }

    /**
     * 获取外连接关联语句列表
     *
     * @param classFullName class name
     * @return join sql list
     */
    public static List<String> getJoinSqlList(String classFullName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getJoinSqlList();
    }

    /**
     * 获取查询时用到的所有 属性
     *
     * @param className class name
     * @return need repository fields
     */
    public static Field[] getViewFields(String className) {
        return ENTITY_MAPPER_POOL.get(className).getViewFields();
    }

    /**
     * 获取查询时用到的所有 属性-列名 映射 带有table name 和 as
     *
     * @param classFullName class name
     * @return field name-column name mapper
     */
    public static Map<String, String> getViewColumnMapperWithAs(String classFullName) {
        return ENTITY_MAPPER_POOL.get(classFullName).getViewColumnMapperWithTableAs();
    }

    /**
     * 获取指定查询时用到的列名
     *
     * @param className class name
     * @param fieldName field name
     * @return view column name
     */
    public static String getViewColumnName(String className, String fieldName) {
        return ENTITY_MAPPER_POOL.get(className).getViewColumnMapper().get(fieldName);
    }

    /**
     * 获取所有业务相关属性
     *
     * @param className class name
     * @return fields
     */
    public static Field[] getBusinessFields(String className) {
        return ENTITY_MAPPER_POOL.get(className).getBusinessFields();
    }

    /**
     * 获取关键业务相关属性
     *
     * @param className class name
     * @return fields
     */
    public static Field[] getKeyBusinessFields(String className) {
        return ENTITY_MAPPER_POOL.get(className).getKeyBusinessFields();
    }

    /**
     * 获取属性的业务名称
     *
     * @param className class name
     * @param fieldName field name
     * @return business name
     */
    public static String getBusinessName(String className, String fieldName) {
        return ENTITY_MAPPER_POOL.get(className).getBusinessMapper().get(fieldName);
    }

    /**
     * 初始化
     *
     * @param context application context
     */
    public static void init(ApplicationContext context) {
        if (!COMPLETED.get()) {
            Map<String, Object> beans = context.getBeansWithAnnotation(Entity.class);
            beans.forEach((name, bean) -> {
                Class clazz = bean.getClass();
                ENTITY_MAPPER_POOL.put(clazz.getName(), EntityMapper.newInstance(clazz));
            });

            COMPLETED.compareAndSet(false, true);
        }
    }
}
