package org.hunter.pocket.model;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.annotation.OneToMany;
import org.hunter.pocket.constant.AnnotationType;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.constant.StreamPredicates;
import org.hunter.pocket.exception.MapperException;
import org.hunter.pocket.utils.CommonUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan
 */
class EntityMapper {

    private String tableName;
    private String uuidGenerator;
    private Map<String, AnnotationMapper> fieldMapper;
    private Field[] repositoryFields;
    private List<String> repositoryColumnNames;
    private Map<String, String> repositoryColumnMapper;
    private Map<String, String> viewColumnMapper;
    private List<String> joinSqlList;

    private EntityMapper() {
    }

    String getTableName() {
        return tableName;
    }

    String getUuidGenerator() {
        return uuidGenerator;
    }

    Map<String, AnnotationMapper> getFieldMapper() {
        return fieldMapper;
    }

    Field[] getRepositoryFields() {
        return repositoryFields;
    }

    public List<String> getRepositoryColumnNames() {
        return repositoryColumnNames;
    }

    Map<String, String> getRepositoryColumnMapper() {
        return repositoryColumnMapper;
    }

    Map<String, String> getViewColumnMapper() {
        return viewColumnMapper;
    }

    List<String> getJoinSqlList() {
        return joinSqlList;
    }

    private EntityMapper(String tableName, String uuidGenerator, Map<String, AnnotationMapper> fieldMapper,
                         Field[] repositoryFields, List<String> repositoryColumnNames, Map<String, String> repositoryColumnMapper,
                         Map<String, String> viewColumnMapper, List<String> joinSqlList) {
        this.tableName = tableName;
        this.uuidGenerator = uuidGenerator;
        this.fieldMapper = fieldMapper;
        this.repositoryFields = repositoryFields;
        this.repositoryColumnNames = repositoryColumnNames;
        this.repositoryColumnMapper = repositoryColumnMapper;
        this.viewColumnMapper = viewColumnMapper;
        this.joinSqlList = joinSqlList;
    }

    public static EntityMapper newInstance(Class clazz) {
        Entity entity = (Entity) clazz.getAnnotation(Entity.class);
        String tableName;
        String uuidGenerator;
        if (entity != null) {
            uuidGenerator = entity.uuidGenerator();
            tableName = entity.table();
        } else {
            throw new MapperException(String.format("%s: 未找到 : @Entity 注解。", clazz.getName()));
        }
        Field[] superWithAnnotationFields = Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(StreamPredicates.COLUMN_MAPPING_PREDICATE).toArray(Field[]::new);
        Field[] ownWithAnnotationFields = Arrays.stream(clazz.getDeclaredFields()).filter(StreamPredicates.COLUMN_MAPPING_PREDICATE).toArray(Field[]::new);
        Field[] withAnnotationFields = (Field[]) CommonUtils.combinedArray(superWithAnnotationFields, ownWithAnnotationFields);

        Map<String, AnnotationMapper> fieldMapper = new LinkedHashMap<>(16);
        List<Field> repositoryFields = new LinkedList<>();
        List<String> repositoryColumnNames = new LinkedList<>();
        Map<String, String> repositoryColumnMapper = new LinkedHashMap<>(16);
        Map<String, String> viewColumnMapper = new LinkedHashMap<>(16);
        List<String> joinSqlList = new LinkedList<>();

        for (Field field : withAnnotationFields) {
            String filedName = field.getName();
            Column column = field.getAnnotation(Column.class);
            Join join = field.getAnnotation(Join.class);
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);

            if (column != null) {
                fieldMapper.put(filedName, new AnnotationMapper(AnnotationType.COLUMN, column));
                repositoryFields.add(field);
                repositoryColumnNames.add(column.name());
                repositoryColumnMapper.put(filedName, column.name());
                viewColumnMapper.put(filedName, tableName + CommonSql.DOT + column.name());
            } else if (join != null) {
                String bridgeColumnSurname = join.columnSurname().trim();
                String joinTableSurname = join.joinTableSurname().trim();
                String joinTableName = join.joinTable();
                String bridgeColumnName = join.bridgeColumn();
                String destinationColumn = join.destinationColumn();
                fieldMapper.put(filedName, new AnnotationMapper(AnnotationType.JOIN, join));
                viewColumnMapper.put(filedName, joinTableSurname
                        + CommonSql.DOT
                        + destinationColumn
                        + CommonSql.AS
                        + bridgeColumnSurname);
                joinSqlList.add(join.joinMethod().getId()
                        + joinTableName + CommonSql.AS + joinTableSurname
                        + CommonSql.ON
                        + tableName + CommonSql.DOT + join.columnName()
                        + CommonSql.EQUAL_TO
                        + joinTableSurname + CommonSql.DOT + bridgeColumnName);
            } else if (oneToMany != null) {
                fieldMapper.put(filedName, new AnnotationMapper(AnnotationType.ONE_TO_MANY, oneToMany));
            } else if (manyToOne != null) {
                fieldMapper.put(filedName, new AnnotationMapper(AnnotationType.MANY_TO_ONE, manyToOne));
                repositoryFields.add(field);
                repositoryColumnNames.add(manyToOne.columnName());
                repositoryColumnMapper.put(filedName, manyToOne.columnName());
                viewColumnMapper.put(filedName, tableName + CommonSql.DOT + manyToOne.columnName());
            }
        }
        return new EntityMapper(tableName, uuidGenerator, fieldMapper, repositoryFields.toArray(new Field[0]), repositoryColumnNames, repositoryColumnMapper, viewColumnMapper, joinSqlList);
    }

    static class AnnotationMapper {
        private AnnotationType annotationType;
        private Annotation annotation;

        AnnotationMapper(AnnotationType annotationType, Annotation annotation) {
            this.annotationType = annotationType;
            this.annotation = annotation;
        }

        AnnotationType getAnnotationType() {
            return annotationType;
        }

        public Annotation getAnnotation() {
            return annotation;
        }
    }
}
