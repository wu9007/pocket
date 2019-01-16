package org.homo.pocket.criteria;

import java.lang.reflect.Field;

/**
 * @author wujianchuan 2019/1/14
 */
public class FieldMapper {
    private String fieldName;
    private String columnName;
    private Field field;

    private FieldMapper(String fieldName, String columnName, Field field) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.field = field;
    }

    public FieldMapper(FieldMapper fieldMapper) {
        this.fieldName = fieldMapper.getFieldName();
        this.columnName = fieldMapper.getColumnName();
        this.field = fieldMapper.getField();
    }

    public static FieldMapper newInstance(String fieldName, String columnName, Field field) {
        return new FieldMapper(fieldName, columnName, field);
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
