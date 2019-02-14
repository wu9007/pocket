package org.hunter.pocket.criteria;

/**
 * @author wujianchuan 2019/1/21
 */
public interface SqlBean {

    /**
     * 获取作为条件传递给`Criteria`或`Modern`的值
     *
     * @return target
     */
    Object getTarget();
}
