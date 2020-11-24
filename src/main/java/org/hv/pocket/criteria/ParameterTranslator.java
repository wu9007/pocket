package org.hv.pocket.criteria;

/**
 * @author wujianchuan 2019/1/21
 */
public class ParameterTranslator implements SqlBean {
    private final String source;
    private Object target;

    private ParameterTranslator(String source, Object target) {
        this.source = source;
        this.target = target;
    }

    public static ParameterTranslator newInstance(String source, Object target) {
        return new ParameterTranslator(source, target);
    }

    public static ParameterTranslator newInstance(Object target) {
        return new ParameterTranslator(null, target);
    }

    @Override
    public String getSource() {
        return this.source;
    }

    @Override
    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public boolean getEncrypted() {
        return false;
    }

    @Override
    public void setEncrypted(boolean encrypted) {
        throw new IllegalAccessError("加密字段不可被用作参数");
    }
}
