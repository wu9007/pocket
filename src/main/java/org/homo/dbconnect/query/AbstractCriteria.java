package org.homo.dbconnect.query;

import org.homo.core.annotation.Entity;
import org.homo.dbconnect.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/1/10
 */
abstract class AbstractCriteria {

    static ReflectUtils reflectUtils = ReflectUtils.getInstance();
    Field[] fields;
    Map<String, Field> fieldsMap = new ConcurrentHashMap<>(15);

    Class clazz;
    StringBuilder sql = new StringBuilder("SELECT ");
    StringBuilder restrictionsSql = new StringBuilder();

    AbstractCriteria(Class clazz) {
        this.clazz = clazz;
        Entity entity = (Entity) clazz.getAnnotation(Entity.class);
        this.fields = reflectUtils.getMappingField(clazz);
        Arrays.stream(this.fields).forEach(field -> fieldsMap.put(field.getName(), field));
        sql.append(reflectUtils.getColumnNames(fields)).append(" FROM ").append(entity.table());
    }
}
