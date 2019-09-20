package org.hv.pocket.criteria;

/**
 * @author wujianchuan 2019/1/21
 */
public class ParameterTranslator implements SqlBean {
    private final Object target;

    private ParameterTranslator(Object target) {
        this.target = target;
    }

    public static ParameterTranslator newInstance(Object target) {
        return new ParameterTranslator(target);
    }

    @Override
    public Object getTarget() {
        return target;
    }
}
