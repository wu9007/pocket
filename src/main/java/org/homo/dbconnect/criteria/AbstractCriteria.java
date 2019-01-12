package org.homo.dbconnect.criteria;

import org.homo.core.annotation.Entity;
import org.homo.dbconnect.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public abstract class AbstractCriteria {

    static ReflectUtils reflectUtils = ReflectUtils.getInstance();
    Field[] fields;
    List<Restrictions> restrictionsList = new ArrayList<>();

    Class clazz;
    StringBuilder sql = new StringBuilder("SELECT ");
    StringBuilder restrictionsSql = new StringBuilder();

    AbstractCriteria(Class clazz) {
        this.clazz = clazz;
        Entity entity = (Entity) clazz.getAnnotation(Entity.class);
        this.fields = reflectUtils.getMappingField(clazz);
//        Arrays.stream(this.fields).forEach(field -> fieldsMap.put(field.getName(), field));
        sql.append(reflectUtils.getColumnNames(fields)).append(" FROM ").append(entity.table());
    }
}
