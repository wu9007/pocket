package org.hv.pocket.model;

import org.hv.pocket.annotation.Entity;
import org.hv.pocket.constant.AnnotationType;
import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.exception.PocketMapperException;
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
     * 获取表名
     *
     * @param className class name
     * @return table name
     */
    public static String getTableName(String className) {
        return getEntityMapper(className).getTableName();
    }

    /**
     * 获取表业务名
     *
     * @param className class name
     * @return table business name
     */
    public static String getBusinessName(String className) {
        return getEntityMapper(className).getTableBusinessName();
    }

    /**
     * 获取主键列明
     *
     * @param className class name
     * @return identify column name
     */
    public static String getIdentifyColumnName(String className) {
        return getEntityMapper(className).getIdentifyColumnName();
    }

    /**
     * 获取主键属性名
     *
     * @param className class name
     * @return field simple name
     */
    public static String getIdentifyFieldName(String className) {
        return getEntityMapper(className).getIdentifyField().getName();
    }

    /**
     * 获取主键生成策略
     *
     * @param className class name
     * @return identify generator
     */
    public static String getIdentifyGenerationType(String className) {
        return getEntityMapper(className).getGenerationType();
    }

    /**
     * 获取属性上的注解类型
     *
     * @param className class name
     * @param fieldName field name
     * @return annotation type
     */
    public static AnnotationType getAnnotationType(String className, String fieldName) {
        EntityMapper entityMapper = getEntityMapper(className);
        EntityMapper.FieldData fieldData = getFieldData(entityMapper, fieldName);
        return fieldData.getAnnotationType();
    }

    /**
     * 获取属性上的注解
     *
     * @param className class name
     * @param fieldName field name
     * @return annotation
     */
    public static Annotation getAnnotation(String className, String fieldName) {
        EntityMapper entityMapper = getEntityMapper(className);
        EntityMapper.FieldData fieldData = getFieldData(entityMapper, fieldName);
        return fieldData.getAnnotation();
    }

    /**
     * 获取属性
     *
     * @param className class name
     * @param fieldName field name
     * @return field
     */
    public static Field getField(String className, String fieldName) {
        EntityMapper entityMapper = getEntityMapper(className);
        EntityMapper.FieldData fieldData = getFieldData(entityMapper, fieldName);
        return fieldData.getField();
    }

    /**
     * 获取需要持久化的属性
     *
     * @param className class name
     * @return need repository fields
     */
    public static Field[] getRepositoryFields(String className) {
        return getEntityMapper(className).getRepositoryFields();
    }

    /**
     * 获取徐持久化的列名
     *
     * @param className class name
     * @return column names
     */
    public static List<String> getRepositoryColumnNames(String className) {
        return getEntityMapper(className).getRepositoryColumnNames();
    }

    /**
     * 获取指定徐持久化的列名
     *
     * @param className class name
     * @param fieldName field name
     * @return column name
     */
    public static String getRepositoryColumnName(String className, String fieldName) {
        return getEntityMapper(className).getRepositoryColumnMapper().get(fieldName);
    }

    /**
     * 获取外连接关联语句列表
     *
     * @param className class name
     * @return join sql list
     */
    public static List<String> getJoinSqlList(String className) {
        return getEntityMapper(className).getJoinSqlList();
    }

    /**
     * 获取查询时用到的所有 属性
     *
     * @param className class name
     * @return need repository fields
     */
    public static Field[] getViewFields(String className) {
        return getEntityMapper(className).getViewFields();
    }

    /**
     * 获取查询时用到的所有 属性-列名 映射 带有table name 和 as
     *
     * @param className class name
     * @return field name-column name mapper
     */
    public static Map<String, String> getViewColumnMapperWithAs(String className) {
        return getEntityMapper(className).getViewColumnMapperWithTableAs();
    }

    /**
     * 获取指定查询时用到的列名
     *
     * @param className class name
     * @param fieldName field name
     * @return view column name
     */
    public static String getViewColumnName(String className, String fieldName) {
        return getEntityMapper(className).getViewColumnMapper().get(fieldName);
    }

    /**
     * 获取所有业务相关属性
     *
     * @param className class name
     * @return fields
     */
    public static Field[] getBusinessFields(String className) {
        return getEntityMapper(className).getBusinessFields();
    }

    /**
     * 获取关键业务相关属性
     *
     * @param className class name
     * @return fields
     */
    public static Field[] getKeyBusinessFields(String className) {
        return getEntityMapper(className).getKeyBusinessFields();
    }

    /**
     * 获取属性的业务名称
     *
     * @param className class name
     * @param fieldName field name
     * @return business name
     */
    public static String getBusinessName(String className, String fieldName) {
        return getEntityMapper(className).getBusinessMapper().get(fieldName);
    }

    /**
     * 获取一对一的属性
     *
     * @param className class name
     * @return one to one fields
     */
    public static Field[] getOneToOneFields(String className) {
        return getEntityMapper(className).getOneToOneFields();
    }

    /**
     * 获取关联类类型
     *
     * @param className main class name
     * @param fieldName main class field name
     * @return related class
     */
    public static Class<? extends AbstractEntity> getOneToOneClass(String className, String fieldName) {
        return getEntityMapper(className).getOneToOneClassMapper().get(fieldName);
    }

    /**
     * 获取对应关联类的关联属性的名称
     *
     * @param className main class name
     * @param fieldName main class field name
     * @return related field name
     */
    public static String getOneToOneRelationFieldName(String className, String fieldName) {
        return getEntityMapper(className).getOneToOneRelatedFieldMapper().get(fieldName);
    }

    /**
     * 获取一对一本类对应表的关联列对应本类属性的名称
     *
     * @param className main class name
     * @param fieldName main class field name
     * @return own field name
     */
    public static String getOneToOneOwnFieldName(String className, String fieldName) {
        return getEntityMapper(className).getOneToOneOwnFieldMapper().get(fieldName);
    }

    /**
     * 获取一对多的属性
     *
     * @param className class name
     * @return one to many fields
     */
    public static Field[] getOneToMayFields(String className) {
        return getEntityMapper(className).getOneToManyFields();
    }

    /**
     * 获取子类类型
     *
     * @param className main class name
     * @param fieldName main class field name
     * @return children class
     */
    public static Class<? extends AbstractEntity> getDetailClass(String className, String fieldName) {
        return getEntityMapper(className).getOnToManyClassMapper().get(fieldName);
    }

    /**
     * 获取对应子类的关联属性的名称
     *
     * @param className main class name
     * @param fieldName main class field name
     * @return children field name
     */
    public static String getOneToMayDownFieldName(String className, String fieldName) {
        return getEntityMapper(className).getOneToManyDownMapper().get(fieldName);
    }

    /**
     * 获取主表的关联属性
     *
     * @param ownClassName own class name
     * @param upClassName  up class name
     * @return field name
     */
    public static String getManyToOneUpField(String ownClassName, String upClassName) {
        return ENTITY_MAPPER_POOL.get(ownClassName).getManyToOneUpMapper().get(upClassName);
    }

    /**
     * 获取字表关联的主表字段值
     *
     * @param entity        main data
     * @param mainClassName main class name
     * @param childClass    child class name
     * @return main field name
     * @throws IllegalAccessException e
     */
    public static Object getUpBridgeFieldValue(AbstractEntity entity, String mainClassName, Class<? extends AbstractEntity> childClass) throws IllegalAccessException {
        String upBridgeFiledName = MapperFactory.getManyToOneUpField(childClass.getName(), mainClassName);
        Field upBridgeField = MapperFactory.getField(mainClassName, upBridgeFiledName);
        upBridgeField.setAccessible(true);
        return upBridgeField.get(entity);
    }

    /**
     * 获取字段加密方式
     *
     * @param className persistence class name
     * @param fieldName persistence field name
     * @return encrypt model{@link EncryptType}
     */
    public static String getEncryptModel(String className, String fieldName) {
        return getEntityMapper(className).getEncryptModel(fieldName);
    }

    /**
     * 初始化
     *
     * @param context application context
     */
    public static void init(ApplicationContext context) throws PocketMapperException {
        if (!COMPLETED.get()) {
            Map<String, Object> beans = context.getBeansWithAnnotation(Entity.class);
            for (Object value : beans.values()) {
                Class<?> clazz = value.getClass();
                ENTITY_MAPPER_POOL.put(clazz.getName(), EntityMapper.newInstance(clazz));
            }

            COMPLETED.compareAndSet(false, true);
        }
    }

    private static EntityMapper getEntityMapper(String className) {
        EntityMapper entityMapper = ENTITY_MAPPER_POOL.get(className);
        if (entityMapper == null) {
            throw new IllegalArgumentException(String.format("The persistent mapping class cannot be found: %s", className));
        }
        return entityMapper;
    }

    private static EntityMapper.FieldData getFieldData(EntityMapper entityMapper, String fieldName) {
        EntityMapper.FieldData fieldData = entityMapper.getFieldMapper().get(fieldName);
        if (fieldData == null) {
            throw new IllegalArgumentException(String.format("The persistent mapping class: %s's attribute: %s cannot be found", entityMapper.getClass().getName(), fieldName));
        }
        return fieldData;
    }
}
