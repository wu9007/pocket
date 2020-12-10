package org.hv.pocket.model;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.Entity;
import org.hv.pocket.annotation.Identify;
import org.hv.pocket.annotation.Join;
import org.hv.pocket.annotation.ManyToOne;
import org.hv.pocket.annotation.OneToMany;
import org.hv.pocket.annotation.OneToOne;
import org.hv.pocket.annotation.View;
import org.hv.pocket.constant.AnnotationType;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.exception.PocketColumnException;
import org.hv.pocket.exception.PocketIdentifyException;
import org.hv.pocket.exception.PocketMapperException;
import org.hv.pocket.utils.UnderlineHumpTranslator;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author wujianchuan
 */
class EntityMapper {
    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    public static final Predicate<Field> COLUMN_MAPPING_PREDICATE =
            field -> !SERIAL_VERSION_UID.equals(field.getName())
                    &&
                    ((field.getAnnotation(Column.class) != null
                            || field.getAnnotation(OneToOne.class) != null
                            || field.getAnnotation(ManyToOne.class) != null
                            || field.getAnnotation(OneToMany.class) != null
                            || field.getAnnotation(Join.class) != null
                    ));

    private final String tableName;
    private final String tableBusinessName;
    private final String identifyColumnName;
    private final Field identifyField;
    private final String generationType;
    private final Map<String, FieldData> fieldMapper;

    private final Field[] repositoryFields;
    private final List<String> repositoryColumnNames;
    private final Map<String, String> repositoryColumnMapper;
    private final Map<String, String> encryptMapper;

    private final Field[] viewFields;
    private final Map<String, String> viewColumnMapperWithTableAs;
    private final Map<String, String> viewColumnMapper;

    private final Field[] businessFields;
    private final Field[] keyBusinessFields;
    private final Map<String, String> businessMapper;

    private final Field[] oneToOneFields;
    private final Map<String, Class<? extends AbstractEntity>> oneToOneClassMapper;
    private final Map<String, String> oneToOneRelatedFieldMapper;
    private final Map<String, String> oneToOneOwnFieldMapper;

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
    private final Map<String, String> joinSqlMap;

    String getTableName() {
        return tableName;
    }

    public String getTableBusinessName() {
        return tableBusinessName;
    }

    public String getIdentifyColumnName() {
        return identifyColumnName;
    }

    public Field getIdentifyField() {
        return identifyField;
    }

    public String getGenerationType() {
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

    public Field[] getOneToOneFields() {
        return oneToOneFields;
    }

    public Map<String, Class<? extends AbstractEntity>> getOneToOneClassMapper() {
        return oneToOneClassMapper;
    }

    public Map<String, String> getOneToOneRelatedFieldMapper() {
        return oneToOneRelatedFieldMapper;
    }

    public Map<String, String> getOneToOneOwnFieldMapper() {
        return oneToOneOwnFieldMapper;
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
        return new ArrayList<>(joinSqlMap.values());
    }

    String getEncryptModel(String fieldName) {
        return this.encryptMapper.get(fieldName);
    }

    public static EntityMapper newInstance(Class<?> clazz) throws PocketMapperException {
        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName;
        String tableBusinessName;
        if (entity != null) {
            tableName = entity.table();
            tableBusinessName = entity.businessName();
        } else {
            View view = clazz.getAnnotation(View.class);
            if (view != null) {
                entity = View.class.getAnnotation(Entity.class);
                tableName = entity.table();
                tableBusinessName = entity.businessName();
            } else {
                throw new PocketMapperException(String.format("%s: 未找到 : @Entity 注解。", clazz.getName()));
            }
        }
        List<Field> allFields = new ArrayList<>();
        for (Class<?> superClass = clazz; superClass != null && superClass != Object.class && superClass != AbstractEntity.class; superClass = superClass.getSuperclass()) {
            allFields.addAll(Arrays.stream(superClass.getDeclaredFields()).filter(COLUMN_MAPPING_PREDICATE).collect(Collectors.toList()));
        }
        Field[] withAnnotationFields = new Field[allFields.size()];
        allFields.toArray(withAnnotationFields);

        return buildMapper(tableName, tableBusinessName, withAnnotationFields);
    }

    private static EntityMapper buildMapper(String tableName, String tableBusinessName, Field[] withAnnotationFields) throws PocketMapperException {
        Field identifyField = null;
        String identifyColumnName = null;
        String generationType = null;
        Map<String, FieldData> fieldMapper = new LinkedHashMap<>(16);
        Map<String, String> repositoryColumnMapper = new LinkedHashMap<>(16), viewColumnMapperWithTableAs = new LinkedHashMap<>(16), viewColumnMapper = new LinkedHashMap<>(16), oneToManyDownMapper = new LinkedHashMap<>(16), manyToOneUpMapper = new LinkedHashMap<>(16), businessMapper = new LinkedHashMap<>(16), encryptMapper = new HashMap<>(8), oneToOneRelatedFieldMapper = new LinkedHashMap<>(4), oneToOneOwnFieldMapper = new LinkedHashMap<>(4);
        List<Field> repositoryFields = new LinkedList<>(), viewFields = new LinkedList<>(), businessFields = new LinkedList<>(), keyBusinessFields = new LinkedList<>(), oneToManyFields = new LinkedList<>(), oneToOneFields = new LinkedList<>();
        Map<String, Class<? extends AbstractEntity>> onToManyClassMapper = new LinkedHashMap<>(16), oneToOneClassMapper = new LinkedHashMap<>();
        List<String> repositoryColumnNames = new LinkedList<>();
        // 相同方式关联相同表的不同字段使用AND拼接
        Map<String, String> joinSqlMap = new LinkedHashMap<>(8);
        // 相同方式关联相同表的相同字段不进行拼接
        List<String> joinMethodTableColumnList = new ArrayList<>();

        Identify identify = null;
        for (Field field : withAnnotationFields) {
            String filedName = field.getName();
            Column column = field.getAnnotation(Column.class);
            Join join = field.getAnnotation(Join.class);
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            if (column != null) {
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.COLUMN, column));
                repositoryFields.add(field);
                String columnName = getColumnName(filedName, column.name());
                repositoryColumnNames.add(columnName);
                repositoryColumnMapper.put(filedName, columnName);
                viewFields.add(field);
                viewColumnMapperWithTableAs.put(filedName, tableName + CommonSql.DOT + columnName);
                viewColumnMapper.put(filedName, columnName);
                pushBusiness(businessFields, keyBusinessFields, businessMapper, field, filedName, column.businessName(), column.flagBusiness());

                if (identify == null) {
                    identify = field.getAnnotation(Identify.class);
                    if (identify != null) {
                        identifyField = field;
                        identifyColumnName = columnName;
                        generationType = identify.strategy();
                    }
                } else if (field.getAnnotation(Identify.class) != null) {
                    throw new PocketIdentifyException("Multiple identify fields detected.");
                }

                if (!StringUtils.isEmpty(column.encryptMode())) {
                    if (String.class.equals(field.getType())) {
                        encryptMapper.put(filedName, column.encryptMode());
                    } else {
                        throw new PocketColumnException("The encrypted field must be of type string.");
                    }
                }
            } else if (join != null) {
                String bridgeColumnSurname = join.columnSurname().trim(), joinTableSurname = join.joinTableSurname().trim(), joinTableName = join.joinTable(), bridgeColumnName = join.bridgeColumn(), destinationColumn = join.destinationColumn();
                String columnName = getColumnName(filedName, join.columnName());
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.JOIN, join));
                viewFields.add(field);
                viewColumnMapperWithTableAs.put(filedName,
                        joinTableSurname + CommonSql.DOT + destinationColumn
                                + CommonSql.AS
                                + bridgeColumnSurname);
                viewColumnMapper.put(filedName, bridgeColumnSurname);
                String joinMethodTableColumnStr = join.joinMethod() + "-" + joinTableSurname + "-" + join.bridgeColumn();
                if (!joinMethodTableColumnList.contains(joinMethodTableColumnStr)) {
                    String joinMapKey = join.joinMethod() + "-" + joinTableSurname;
                    String joinSql = joinSqlMap.get(joinMapKey);
                    if (joinSql != null) {
                        joinSql += CommonSql.AND;
                    } else {
                        joinSql = join.joinMethod().getId()
                                + joinTableName + CommonSql.BLANK_SPACE + joinTableSurname
                                + CommonSql.ON;
                    }
                    joinSql += (columnName.contains(CommonSql.DOT) ? columnName : tableName + CommonSql.DOT + columnName)
                            + CommonSql.EQUAL_TO
                            + joinTableSurname + CommonSql.DOT + bridgeColumnName;
                    joinSqlMap.put(joinMapKey, joinSql);
                    joinMethodTableColumnList.add(joinMethodTableColumnStr);
                }
                pushBusiness(businessFields, keyBusinessFields, businessMapper, field, filedName, join.businessName(), join.flagBusiness());

                if (!StringUtils.isEmpty(join.encryptMode())) {
                    if (String.class.equals(field.getType())) {
                        encryptMapper.put(filedName, join.encryptMode());
                    } else {
                        throw new PocketColumnException("The encrypted field must be of type string.");
                    }
                }
            } else if (oneToMany != null) {
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.ONE_TO_MANY, oneToMany));
                pushBusiness(businessFields, keyBusinessFields, businessMapper, field, filedName, oneToMany.businessName(), oneToMany.flagBusiness());
                oneToManyFields.add(field);
                onToManyClassMapper.put(filedName, oneToMany.clazz());
                oneToManyDownMapper.put(filedName, oneToMany.bridgeField());
            } else if (manyToOne != null) {
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.MANY_TO_ONE, manyToOne));
                repositoryFields.add(field);
                String columnName = getColumnName(filedName, manyToOne.columnName());
                repositoryColumnNames.add(columnName);
                repositoryColumnMapper.put(filedName, columnName);
                viewFields.add(field);
                viewColumnMapperWithTableAs.put(filedName, tableName + CommonSql.DOT + columnName);
                viewColumnMapper.put(filedName, columnName);
                manyToOneUpMapper.put(manyToOne.clazz().getName(), manyToOne.upBridgeField());
            } else if (oneToOne != null) {
                fieldMapper.put(filedName, new FieldData(field, AnnotationType.ONE_TO_ONE, oneToOne));
                oneToOneFields.add(field);
                Class<?> oneClazz = field.getType();
                if (!AbstractEntity.class.isAssignableFrom(oneClazz)) {
                    throw new PocketColumnException("The declaring clazz of on-to-one field must be subclass of AbstractEntity.");
                }
                oneToOneClassMapper.put(filedName, (Class<? extends AbstractEntity>) field.getType());
                oneToOneRelatedFieldMapper.put(filedName, oneToOne.relatedField());
                oneToOneOwnFieldMapper.put(filedName, oneToOne.ownField());
            }
        }
        return new EntityMapper(tableName, tableBusinessName, identifyField, identifyColumnName, generationType, fieldMapper,
                repositoryFields.toArray(new Field[0]), repositoryColumnNames, repositoryColumnMapper,
                viewFields.toArray(new Field[0]), viewColumnMapperWithTableAs, viewColumnMapper,
                businessFields.toArray(new Field[0]), keyBusinessFields.toArray(new Field[0]), businessMapper,
                oneToManyFields.toArray(new Field[0]), onToManyClassMapper, oneToManyDownMapper,
                manyToOneUpMapper, joinSqlMap, encryptMapper,
                oneToOneFields.toArray(new Field[0]), oneToOneClassMapper, oneToOneRelatedFieldMapper, oneToOneOwnFieldMapper
        );
    }

    private static String getColumnName(String filedName, String columnName) {
        if (columnName.isEmpty()) {
            columnName = UnderlineHumpTranslator.humpToUnderline(filedName);
        }
        return columnName;
    }

    private EntityMapper(String tableName, String tableBusinessName, Field identifyFile, String identifyColumnName, String generationType, Map<String, FieldData> fieldMapper,
                         Field[] repositoryFields, List<String> repositoryColumnNames, Map<String, String> repositoryColumnMapper,
                         Field[] viewFields, Map<String, String> viewColumnMapperWithTableAs, Map<String, String> viewColumnMapper,
                         Field[] businessFields, Field[] keyBusinessFields, Map<String, String> businessMapper,
                         Field[] oneToManyFields, Map<String, Class<? extends AbstractEntity>> onToManyClassMapper, Map<String, String> oneToManyDownMapper,
                         Map<String, String> manyToOneUpMapper, Map<String, String> joinSqlMap, Map<String, String> encryptMapper,
                         Field[] oneToOneFields, Map<String, Class<? extends AbstractEntity>> oneToOneClassMapper, Map<String, String> oneToOneRelatedFieldMapper, Map<String, String> oneToOneOwnFieldMapper
    ) {
        this.tableName = tableName;
        this.tableBusinessName = tableBusinessName;
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
        this.joinSqlMap = joinSqlMap;
        this.encryptMapper = encryptMapper;
        this.oneToOneFields = oneToOneFields;
        this.oneToOneClassMapper = oneToOneClassMapper;
        this.oneToOneRelatedFieldMapper = oneToOneRelatedFieldMapper;
        this.oneToOneOwnFieldMapper = oneToOneOwnFieldMapper;
    }

    private static void pushBusiness(List<Field> businessFields, List<Field> keyBusinessFields, Map<String, String> businessMapper, Field field, String filedName, String businessName, boolean flagBusiness) throws PocketMapperException {
        if (businessMapper.containsValue(businessName)) {
            throw new PocketMapperException(String.format("Duplicate businessName -> %s in class -> %s.", businessName, field.getDeclaringClass()));
        }
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
