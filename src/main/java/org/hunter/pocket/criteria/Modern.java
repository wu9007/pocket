package org.hunter.pocket.criteria;

import org.hunter.pocket.exception.CriteriaException;
import org.hunter.pocket.exception.ErrorMessage;

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
    private final Boolean withPoEl;

    private Modern(String source, Object target) {
        this.source = source;
        this.target = target;
        this.withPoEl = false;
    }

    private Modern(String poEl) {
        this.poEl = poEl;
        this.withPoEl = true;
    }

    public static Modern set(String source, Object target) {
        return new Modern(source, target);
    }


    public static Modern setWithPoEl(String poEl) {
        return new Modern(poEl);
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

    private Boolean getWithPoEl() {
        return withPoEl;
    }

    private void setTarget(Object target) {
        this.target = target;
    }

    String parse(Map<String, FieldMapper> fieldMapper, List<ParameterTranslator> parameters, Map<String, Object> parameterMap) {
        if (this.getWithPoEl()) {
            String sql = poEl;
            Matcher fieldMatcher = fieldPattern.matcher(this.poEl);
            Matcher valueMatcher = valuePattern.matcher(this.poEl);
            String fieldName = null;
            try {
                while (fieldMatcher.find()) {
                    fieldName = fieldMatcher.group().substring(1);
                    sql = sql.replace(fieldMatcher.group(), fieldMapper.get(fieldName).getColumnName());
                }
            } catch (NullPointerException e) {
                throw new CriteriaException(String.format(ErrorMessage.POCKET_ILLEGAL_FIELD_EXCEPTION, fieldName));
            }
            while (valueMatcher.find()) {
                sql = sql.replace(valueMatcher.group(), "?");
                parameters.add(ParameterTranslator.newInstance(parameterMap.get(valueMatcher.group().substring(1))));
            }
            return sql;
        } else {
            parameters.add(ParameterTranslator.newInstance(this.target));
            return fieldMapper.get(this.getSource()).getColumnName() + " = ?";
        }
    }
}
