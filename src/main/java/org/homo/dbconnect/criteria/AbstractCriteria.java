package org.homo.dbconnect.criteria;

import org.homo.dbconnect.annotation.Entity;
import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.inventory.FieldTypeStrategy;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.homo.dbconnect.utils.ReflectUtils.FIND_CHILDREN;

/**
 * @author wujianchuan 2019/1/10
 */
abstract class AbstractCriteria {

    ReflectUtils reflectUtils = ReflectUtils.getInstance();
    FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();
    SqlFactory sqlFactory = SqlFactory.getInstance();

    Class clazz;
    Transaction transaction;
    AbstractDatabaseConfig databaseConfig;

    Field[] fields;
    Field[] childrenFields;
    Map<String, FieldMapper> fieldMapper;
    List<Restrictions> restrictionsList = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT ");

    AbstractCriteria(Class clazz, Transaction transaction, AbstractDatabaseConfig databaseConfig) {
        this.clazz = clazz;
        this.transaction = transaction;
        this.databaseConfig = databaseConfig;
        this.fields = reflectUtils.getMappingField(clazz);
        this.childrenFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(FIND_CHILDREN)
                .toArray(Field[]::new);
        this.fieldMapper = reflectUtils.getFieldMapperMap(clazz);

        Entity entity = (Entity) clazz.getAnnotation(Entity.class);
        this.sql.append(reflectUtils.getColumnNames(fields)).append(" FROM ").append(entity.table());
    }
}
