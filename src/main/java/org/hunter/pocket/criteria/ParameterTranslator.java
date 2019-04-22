package org.hunter.pocket.criteria;

import org.hunter.pocket.exception.ErrorMessage;
import org.hunter.pocket.exception.QueryException;

/**
 * @author wujianchuan 2019/1/21
 */
public class ParameterTranslator implements SqlBean {
    private String source;
    private Object target;

    private ParameterTranslator(String source, Object target) {
        this.source = source;
        this.target = target;
    }

    private ParameterTranslator(Object target) {
        this.target = target;
    }

    public static ParameterTranslator newInstance(String source, Object target) {
        return new ParameterTranslator(source, target);
    }

    public static ParameterTranslator newInstance(Object target) {
        if (target != null) {
            return new ParameterTranslator(target);
        } else {
            throw new QueryException(ErrorMessage.POCKET_MISS_PARAM_EXCEPTION);
        }
    }

    public String getSource() {
        return source;
    }

    @Override
    public Object getTarget() {
        return target;
    }
}
