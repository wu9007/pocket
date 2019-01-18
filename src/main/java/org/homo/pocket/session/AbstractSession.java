package org.homo.pocket.session;

import org.homo.pocket.annotation.ManyToOne;
import org.homo.pocket.annotation.OneToMany;
import org.homo.pocket.model.BaseEntity;
import org.homo.pocket.config.DatabaseNodeConfig;
import org.homo.pocket.utils.FieldTypeStrategy;
import org.homo.pocket.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;

import static org.homo.pocket.utils.ReflectUtils.FIND_CHILDREN;
import static org.homo.pocket.utils.ReflectUtils.FIND_PARENT;

/**
 * @author wujianchuan 2019/1/9
 */
abstract class AbstractSession implements Session {

    private Logger logger = LoggerFactory.getLogger(AbstractSession.class);

    Connection connection;
    Transaction transaction;
    DatabaseNodeConfig databaseNodeConfig;
    FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();
    ReflectUtils reflectUtils = ReflectUtils.getInstance();

    AbstractSession(DatabaseNodeConfig databaseNodeConfig) {
        this.databaseNodeConfig = databaseNodeConfig;
    }

    void showSql(String sql) {
        if (this.databaseNodeConfig.getShowSql()) {
            this.logger.info("SQL: {}", sql);
        }
    }

    void adoptChildren(BaseEntity entity) throws Exception {
        Field[] childrenFields = Arrays.stream(entity.getClass().getDeclaredFields()).filter(FIND_CHILDREN).toArray(Field[]::new);
        if (childrenFields.length > 0) {
            for (Field childField : childrenFields) {
                childField.setAccessible(true);
                OneToMany oneToMany = childField.getAnnotation(OneToMany.class);
                Collection child = (Collection) childField.get(entity);
                if (child != null && child.size() > 0) {
                    Field[] detailFields = childField.getAnnotation(OneToMany.class).clazz().getDeclaredFields();
                    Field mappingField = Arrays.stream(detailFields)
                            .filter(FIND_PARENT)
                            .filter(field -> oneToMany.name().equals(field.getAnnotation(ManyToOne.class).name()))
                            .findFirst().orElseThrow(() -> new NullPointerException("子表实体未配置ManyToOne(name = \"" + oneToMany.name() + "\")注解"));
                    for (Object detail : child) {
                        mappingField.setAccessible(true);
                        mappingField.set(detail, entity.getUuid());
                        this.save((BaseEntity) detail);
                    }
                }
            }
        }
    }

    void statementApplyValue(BaseEntity entity, Field[] fields, PreparedStatement preparedStatement) throws Exception {
        for (int valueIndex = 0; valueIndex < fields.length; valueIndex++) {
            Field field = fields[valueIndex];
            field.setAccessible(true);
            preparedStatement.setObject(valueIndex + 1, field.get(entity));
        }
    }
}
