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

import static org.hunter.pocket.constant.StreamPredicates.CHILDREN_MAPPING_PREDICATE;

/**
 * @author wujianchuan
 */
class EntityMapper {

    private int tableId;
    private String tableName;
    private String uuidGenerator;
    private Map<String, FieldData> fieldMapper;

    private Field[] repositoryFields;
    private List<String> repositoryColumnNames;
    private Map<String, String> repositoryColumnMapper;

    private Field[] viewFields;
    private Map<String, String> viewColumnMapperWithTableAs;
    private Map<String, String> viewColumnMapper;

    private Field[] businessFields;
    private Field[] keyBusinessFields;
    private Map<String, String> businessMapper;

    private Field[] oneToManyFields;
    private Map<String, Class> onToManyClassMapper;
    /**
     * key->主类属性名，value->子类属性名
     */
    private Map<String, String> oneToManyDownMapper;
    /**
     * key->主类名称，value->关联的主类属性名
     */
    private Map<String, String> manyToOneUpMapper;
    private List<String> joinSqlList;

    int getTableId() {
        return tableId;
    }

    String getTableName() {
        return tableName;
    }

    String getUuidGenerator() {
        return uuidGenerator;
    }

    Map<String, FieldData> getFieldMapper() {
        return fieldMapper;
    }

    Field[] getRepositoryFields() {
        return repositoryFields;
    }

    List<String> getRepositoryColumnNames() {
        return repositoryColumnNames;
    }

    Map<String, String> getRepositoryColumnMapper() {
        return repositoryColumnMapper;
    }

    Field[] getViewFields() {
        return viewFields;
    }

    Map<String, String> getViewColumnMapperWithTableAs() {
        return viewColumnMapperWithTableAs;
    }

    Map<String, String> getViewColumnMapper() {
        return viewColumnMapper;
    }

    Field[] getBusinessFields() {
        return businessFields;
    }

    Field[] getKeyBusinessFields() {
        return keyBusinessFields;
    }

    Map<String, String> getBusinessMapper() {
        return businessMapper;
    }

    Field[] getOneToManyFields() {
        return oneToManyFields;
    }

    Map<String, Class> getOnToManyClassMapper() {
        return onToManyClassMapper;
    }

    Map<String, String> getOneToManyDownMapper() {
        return oneToManyDownMapper;
    }

    Map<String, String> getManyToOneUpMapper() {
        return manyToOneUpMapper;
    }

    List<String> getJoinSqlList() {
        return joinSqlList;
    }

    private EntityMapper(int tableId, String tableName, String uuidGenerator, Map<String, FieldData> fieldMapper,
                         Field[] repositoryFields, List<String> repositoryColumnNames, Map<String, String> repositoryColumnMapper,
                         Field[] viewFields, Map<String, String> viewColumnMapperWithTableAs, Map<String, String> viewColumnMapper,
                         Field[] businessFields, Field[] keyBusinessFields, Map<String, String> businessMapper,
                         Field[] oneToManyFields, Map<String, Class> onToManyClassMapper, Map<String, String> oneToManyDownMapper,
                         Map<String, String> manyToOneUpMapper, List<String> joinSqlList) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.uuidGenerator = uuidGenerator;
        this.fieldMapper = fieldMapper;
        this.repositoryFields = repositoryFields;
        this.repositoryColumnNames = repositoryColumnNames;
        this.repositoryColumnMapper = repositoryColumnMapper;
        this.viewFields = viewFields;
        this.viewColumnMapperWithTableAs = viewColumnMapperWithTableAs;
        this.viewColumnMapper = viewColumnMapper;
        this.businessFields = businessFields;
        this.keyBusinessFields = keyBusinessFields;
        this.businessMapper = businessMapper;
        this.oneToManyFields = oneToManyFields;
        this.onToManyClassMapper = onToManyClassMapper;
        this.oneToManyDownMapper = oneToManyDownMapper;
        this.manyToOneUpMapper = manyToOneUpMapper;
        this.joinSqlList = joinSqlList;
    }

    public static EntityMapper newInstance(Class clazz) {
        Entity entity = (Entity) clazz.getAnnotation(Entity.class);
        String tableName;
        int tableId;
        String uuidGenerator;
        if (entity != null) {
            uuidGenerator = entity.uuidGenerator();
            tableName = entity.table();
            tableId = entity.tableId();
        } else {
            throw new MapperException(String.format("%s: 未找到 : @Entity 注解。", clazz.getName()));
        }
        Field[] superWithAnnotationFields = Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(StreamPredicates.COLUMN_MAPPING_PREDICATE).toArray(Field[]::new);
        Field[] ownWithAnnotationFields = Arrays.stream(clazz.getDeclaredFields()).filter(StreamPredicates.COLUMN_MAPPING_PREDICATE).toArray(Field[]::new);
        Field[] withAnnotationFields = (Field[]) CommonUtils.combinedArray(superWithAnnotationFields, ownWithAnnotationFields);
        Field[] noAnnotationFields = Arrays.stream(clazz.getDeclaredFields()).filter(field -> !Arrays.asList(withAnnotationFields).contains(field)).filter(CHILDREN_MAPPING_PREDICATE).toArray(Field[]::new);

        return buildMapper(tableName, tableId, uuidGenerator, withAnnotationFields, noAnnotationFields);
    }

    private static EntityMapper buildMapper(String tableName, int tableId, String uuidGenerator, Field[] withAnnotationFields, Field[] noAnnotationFields) {
        Map<String, FieldData> fieldMapper = new LinkedHashMap<>(16);
        List<Field> repositoryFields = new LinkedList<>();
        List<String> repositoryColumnNames = new LinkedList<>();
        Map<String, String> repositoryColumnMapper = new LinkedHashMap<>(16);
        Map<String, String> viewColumnMapperWithTableAs = new LinkedHashMap<>(16);
        Map<String, String> viewColumnMapper = new LinkedHashMap<>(16);
        List<Field> businessFields = new LinkedList<>();
        List<Field> keyBusinessFields = new LinkedList<>();
        Map<String, String> businessMapper = new LinkedHashMap<>(16);
        List<Field> oneToManyField = new LinkedList<>();
        Map<String, Class> onToManyClassMapper = new LinkedHashMap<>(16);
        Map<String, String> oneToManyDownMapper = new LinkedHashMap<>(16);
        Map<String, String> manyToOneUpMapper = new LinkedHashMap<>(16);
        List<String> joinSqlList = new LinkedList<>();

        List<Field> viewFields = new LinkedList<>(Arrays.asList(noAnnotationFields));
        for (Field field : withAnnotationFields) {
            String filedName = field.getName();
            Column column = field.getAnnotation(Column.class);
            Join join = field.getAnnotation(Join.class);
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);

            if (column != null) {
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.COLUMN, column));
                repositoryFields.add(field);
                repositoryColumnNames.add(column.name());
                repositoryColumnMapper.put(filedName, column.name());
                viewFields.add(field);
                viewColumnMapperWithTableAs.put(filedName, tableName + CommonSql.DOT + column.name());
                viewColumnMapper.put(filedName, column.name());
                pushBusiness(businessFields, keyBusinessFields, businessMapper, field, filedName, column.businessName(), column.flagBusiness());
            } else if (join != null) {
                String bridgeColumnSurname = join.columnSurname().trim();
                String joinTableSurname = join.joinTableSurname().trim();
                String joinTableName = join.joinTable();
                String bridgeColumnName = join.bridgeColumn();
                String destinationColumn = join.destinationColumn();
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.JOIN, join));
                viewFields.add(field);
                viewColumnMapperWithTableAs.put(filedName,
                        joinTableSurname + CommonSql.DOT + destinationColumn
                                + CommonSql.AS
                                + bridgeColumnSurname);
                viewColumnMapper.put(filedName, bridgeColumnSurname);
                joinSqlList.add(join.joinMethod().getId()
                        + joinTableName + CommonSql.AS + joinTableSurname
                        + CommonSql.ON
                        + (join.columnName().contains(CommonSql.DOT) ? join.columnName() : tableName + CommonSql.DOT + join.columnName())
                        + CommonSql.EQUAL_TO
                        + joinTableSurname + CommonSql.DOT + bridgeColumnName);
                pushBusiness(businessFields, keyBusinessFields, businessMapper, field, filedName, join.businessName(), join.flagBusiness());
            } else if (oneToMany != null) {
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.ONE_TO_MANY, oneToMany));
                pushBusiness(businessFields, keyBusinessFields, businessMapper, field, filedName, oneToMany.businessName(), oneToMany.flagBusiness());
                oneToManyField.add(field);
                onToManyClassMapper.put(filedName, oneToMany.clazz());
                oneToManyDownMapper.put(filedName, oneToMany.bridgeField());
            } else if (manyToOne != null) {
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.MANY_TO_ONE, manyToOne));
                repositoryFields.add(field);
                repositoryColumnNames.add(manyToOne.columnName());
                repositoryColumnMapper.put(filedName, manyToOne.columnName());
                viewFields.add(field);
                viewColumnMapperWithTableAs.put(filedName, tableName + CommonSql.DOT + manyToOne.columnName());
                viewColumnMapper.put(filedName, manyToOne.columnName());
                manyToOneUpMapper.put(manyToOne.clazz().getName(), manyToOne.upBridgeField());
            }
        }
        return new EntityMapper(tableId, tableName, uuidGenerator, fieldMapper,
                repositoryFields.toArray(new Field[0]), repositoryColumnNames, repositoryColumnMapper,
                viewFields.toArray(new Field[0]), viewColumnMapperWithTableAs, viewColumnMapper,
                businessFields.toArray(new Field[0]), keyBusinessFields.toArray(new Field[0]), businessMapper,
                oneToManyField.toArray(new Field[0]), onToManyClassMapper, oneToManyDownMapper,
                manyToOneUpMapper, joinSqlList);
    }

    private static void pushBusiness(List<Field> businessFields, List<Field> keyBusinessFields, Map<String, String> businessMapper, Field field, String filedName, String businessName, boolean flagBusiness) {
        if (businessName.trim().length() > 0) {
            if (flagBusiness) {
                keyBusinessFields.add(field);
            }
            businessFields.add(field);
            businessMapper.put(filedName, businessName);
        }
    }

    static class FieldData {
        Field field;
        private final AnnotationType annotationType;
        private final Annotation annotation;

        FieldData(Field field, AnnotationType annotationType, Annotation annotation) {
            this.field = field;
            this.annotationType = annotationType;
            this.annotation = annotation;
        }

        public Field getField() {
            return field;
        }

        AnnotationType getAnnotationType() {
            return annotationType;
        }

        public Annotation getAnnotation() {
            return annotation;
        }
    }
}
