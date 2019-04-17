package org.hunter.pocket.criteria;

import org.hunter.pocket.exception.CriteriaException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hunter.pocket.constant.RegexString.EL_FIELD_REGEX;
import static org.hunter.pocket.constant.RegexString.SQL_PARAMETER_REGEX;

/**
 * @author wujianchuan 2019/1/21
 */
public class Modern implements SqlBean {
    private final Pattern fieldPattern = Pattern.compile(EL_FIELD_REGEX);
    private final Pattern valuePattern = Pattern.compile(SQL_PARAMETER_REGEX);

    private String source;
    private Object target;
    private String poEl;
    private Boolean withPoEl;

    private Modern(String source, Object target, boolean withPoEl) {
        this.source = source;
        this.target = target;
        this.withPoEl = withPoEl;
    }

    public Modern(String poEl, boolean withPoEl) {
        this.source = source;
        this.poEl = poEl;
        this.withPoEl = withPoEl;
    }

    public static Modern set(String source, Object target) {
        return new Modern(source, target, false);
    }


    public static Modern setWithPoEl(String poEl) {
        return new Modern(poEl, true);
    }

    public String getSource() {
        return source;
    }

    private void setSource(String source) {
        this.source = source;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    public String getPoEl() {
        return this.poEl;
    }

    public Boolean getWithPoEl() {
        return withPoEl;
    }

    private void setTarget(Object target) {
        this.target = target;
    }

    public String parse(Map<String, FieldMapper> fieldMapper, List<ParameterTranslator> parameters, Map<String, Object> parameterMap) {
        if (this.getWithPoEl()) {
            String sql = poEl;
            Matcher fieldMatcher = fieldPattern.matcher(this.poEl);
            Matcher valueMatcher = valuePattern.matcher(this.poEl);
            if (fieldMatcher.find() && valueMatcher.find()) {
                String fieldEl = fieldMatcher.group();
                String valueEl = valueMatcher.group();
                String fieldName = fieldMatcher.group().substring(1);
                String valueName = valueMatcher.group().substring(1);
                String columnName = fieldMapper.get(fieldName).getColumnName();
                sql = columnName + " = " + sql.replace(fieldEl, columnName)
                        .replace(valueEl, "?");
                parameters.add(ParameterTranslator.newInstance(valueName, parameterMap.get(valueName)));
            } else {
                throw new CriteriaException("can not match.");
            }
            return sql;
        } else {
            parameters.add(new ParameterTranslator(this.target));
            return fieldMapper.get(this.getSource()).getColumnName() + " = ?";
        }
    }
}
