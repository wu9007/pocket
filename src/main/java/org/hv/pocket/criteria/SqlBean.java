package org.hv.pocket.criteria;

/**
 * @author wujianchuan 2019/1/21
 */
public interface SqlBean {
    /**
     * 获取字段名
     *
     * @return 字段名
     */
    String getSource();

    /**
     * 设置参数值（例如：加密传递的明文参数）
     *
     * @param target 明文参数
     */
    void setTarget(Object target);

    /**
     * 获取作为条件传递给 {@link Restrictions} {@link Modern} {@link ParameterTranslator} 的值。
     *
     * @return 参数值
     */
    Object getTarget();
}
