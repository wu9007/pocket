package org.hv.pocket.model;

import org.hv.pocket.annotation.*;
import org.hv.pocket.constant.AnnotationType;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.constant.StreamPredicates;
import org.hv.pocket.exception.MapperException;
import org.hv.pocket.exception.PocketIdentifyException;
import org.hv.pocket.utils.CommonUtils;
import org.hv.pocket.identify.GenerationType;
import org.hv.pocket.utils.UnderlineHumpTranslator;

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

    private final int tableId;
    private final String tableName;
    private final String identifyColumnName;
    private final Field identifyField;
    private final GenerationType generationType;
    private final Map<String, FieldData> fieldMapper;

    private final Field[] repositoryFields;
    private final List<String> repositoryColumnNames;
    private final Map<String, String> repositoryColumnMapper;

    private final Field[] viewFields;
    private final Map<String, String> viewColumnMapperWithTableAs;
    private final Map<String, String> viewColumnMapper;

    private final Field[] businessFields;
    private final Field[] keyBusinessFields;
    private final Map<String, String> businessMapper;

    private final Field[] oneToManyFields;
    private final Map<String, Class<? extends AbstractEntity>> onToManyClassMapper;
    /**
     * key->主类属性名，value->子类属性名
     */
    private final Map<String, String> oneToManyDownMapper;
    /**
     * key->主类名称，value->关联的主类属性名
     */
    private final Map<String, String> manyToOneUpMapper;
    private final List<String> joinSqlList;

    int getTableId() {
        return tableId;
    }

    String getTableName() {
        return tableName;
    }

    public String getIdentifyColumnName() {
        return identifyColumnName;
    }

    public Field getIdentifyField() {
        return identifyField;
    }

    public GenerationType getGenerationType() {
        return generationType;
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

    public Map<String, Class<? extends AbstractEntity>> getOnToManyClassMapper() {
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

    public static EntityMapper newInstance(Class<?> clazz) {
        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName;
        int tableId;
        if (entity != null) {
            tableName = entity.table();
            tableId = entity.tableId();
        } else {
            View view = clazz.getAnnotation(View.class);
            if (view != null) {
                entity = View.class.getAnnotation(Entity.class);
                tableName = entity.table();
                tableId = entity.tableId();
            } else {
                throw new MapperException(String.format("%s: 未找到 : @Entity 注解。", clazz.getName()));
            }
        }
        Field[] superWithAnnotationFields = Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(StreamPredicates.COLUMN_MAPPING_PREDICATE).toArray(Field[]::new);
        Field[] ownWithAnnotationFields = Arrays.stream(clazz.getDeclaredFields()).filter(StreamPredicates.COLUMN_MAPPING_PREDICATE).toArray(Field[]::new);
        Field[] withAnnotationFields = (Field[]) CommonUtils.combinedArray(superWithAnnotationFields, ownWithAnnotationFields);

        return buildMapper(tableName, tableId, withAnnotationFields);
    }

    private static EntityMapper buildMapper(String tableName, int tableId, Field[] withAnnotationFields) {
        Field identifyField = null;
        String identifyColumnName = null;
        GenerationType generationType = null;
        Map<String, FieldData> fieldMapper = new LinkedHashMap<>(16);
        Map<String, String> repositoryColumnMapper = new LinkedHashMap<>(16), viewColumnMapperWithTableAs = new LinkedHashMap<>(16), viewColumnMapper = new LinkedHashMap<>(16), oneToManyDownMapper = new LinkedHashMap<>(16), manyToOneUpMapper = new LinkedHashMap<>(16), businessMapper = new LinkedHashMap<>(16);
        List<Field> repositoryFields = new LinkedList<>(), viewFields = new LinkedList<>(), businessFields = new LinkedList<>(), keyBusinessFields = new LinkedList<>(), oneToManyField = new LinkedList<>();
        Map<String, Class<? extends AbstractEntity>> onToManyClassMapper = new LinkedHashMap<>(16);
        List<String> repositoryColumnNames = new LinkedList<>(), joinSqlList = new LinkedList<>();

        Identify identify = null;
        for (Field field : withAnnotationFields) {
            String filedName = field.getName();
            Column column = field.getAnnotation(Column.class);
            Join join = field.getAnnotation(Join.class);
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            if (identify == null) {
                identify = field.getAnnotation(Identify.class);
                if (identify != null) {
                    identifyField = field;
                    identifyColumnName = column.name();
                    generationType = identify.strategy();
                }
            } else if (field.getAnnotation(Identify.class) != null) {
                throw new PocketIdentifyException("Multiple identify fields detected.");
            }
            if (column != null) {
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.COLUMN, column));
                repositoryFields.add(field);
                String columnName = column.name();
                if (columnName.isEmpty()) {
                    columnName = UnderlineHumpTranslator.humpToUnderline(filedName);
                }
                repositoryColumnNames.add(columnName);
                repositoryColumnMapper.put(filedName,columnName);
                viewFields.add(field);
                viewColumnMapperWithTableAs.put(filedName, tableName + CommonSql.DOT + columnName);
                viewColumnMapper.put(filedName, columnName);
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
        if (identify == null && tableId > -1) {
            throw new PocketIdentifyException("Missing identify field.");
        }
        return new EntityMapper(tableId, tableName, identifyField, identifyColumnName, generationType, fieldMapper,
                repositoryFields.toArray(new Field[0]), repositoryColumnNames, repositoryColumnMapper,
                viewFields.toArray(new Field[0]), viewColumnMapperWithTableAs, viewColumnMapper,
                businessFields.toArray(new Field[0]), keyBusinessFields.toArray(new Field[0]), businessMapper,
                oneToManyField.toArray(new Field[0]), onToManyClassMapper, oneToManyDownMapper,
                manyToOneUpMapper, joinSqlList);
    }

    private EntityMapper(int tableId, String tableName, Field identifyFile, String identifyColumnName, GenerationType generationType, Map<String, FieldData> fieldMapper,
                         Field[] repositoryFields, List<String> repositoryColumnNames, Map<String, String> repositoryColumnMapper,
                         Field[] viewFields, Map<String, String> viewColumnMapperWithTableAs, Map<String, String> viewColumnMapper,
                         Field[] businessFields, Field[] keyBusinessFields, Map<String, String> businessMapper,
                         Field[] oneToManyFields, Map<String, Class<? extends AbstractEntity>> onToManyClassMapper, Map<String, String> oneToManyDownMapper,
                         Map<String, String> manyToOneUpMapper, List<String> joinSqlList) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.identifyField = identifyFile;
        this.identifyColumnName = identifyColumnName;
        this.generationType = generationType;
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
