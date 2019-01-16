package org.homo.pocket.criteria;

import org.homo.pocket.annotation.Entity;
import org.homo.pocket.config.DatabaseNodeConfig;
import org.homo.pocket.utils.FieldTypeStrategy;
import org.homo.pocket.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.homo.pocket.utils.ReflectUtils.FIND_CHILDREN;

/**
 * @author wujianchuan 2019/1/10
 */
abstract class AbstractCriteria {

    private Logger logger = LoggerFactory.getLogger(AbstractCriteria.class);
    ReflectUtils reflectUtils = ReflectUtils.getInstance();
    FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();
    SqlFactory sqlFactory = SqlFactory.getInstance();

    Class clazz;
    Connection connection;
    DatabaseNodeConfig databaseConfig;

    Field[] fields;
    Field[] childrenFields;
    Map<String, FieldMapper> fieldMapper;
    List<Restrictions> restrictionsList = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT ");

    AbstractCriteria(Class clazz, Connection connection, DatabaseNodeConfig databaseConfig) {
        this.clazz = clazz;
        this.connection = connection;
        this.databaseConfig = databaseConfig;
        this.fields = reflectUtils.getMappingField(clazz);
        this.childrenFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(FIND_CHILDREN)
                .toArray(Field[]::new);
        this.fieldMapper = reflectUtils.getFieldMapperMap(clazz);

        Entity entity = (Entity) clazz.getAnnotation(Entity.class);
        this.sql.append(reflectUtils.getColumnNames(fields)).append(" FROM ").append(entity.table());
    }

    void before() {
        if (this.databaseConfig.getShowSql()) {
            this.logger.info("SQL: {}", this.sql);
        }
    }

}
