package org.hunter.pocket.criteria;

/**
 * @author wujianchuan 2019/1/21
 */
public class ParameterTranslator implements SqlBean {
    private String source;
    private Object target;

    public ParameterTranslator(String source, Object target) {
        this.source = source;
        this.target = target;
    }

    public static ParameterTranslator set(String source, Object target) {
        return new ParameterTranslator(source, target);
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

    private void setTarget(Object target) {
        this.target = target;
    }
}
