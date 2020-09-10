package org.hv.pocket.session;

/**
 * @author wujianchuan 2019/1/1
 */
public interface Transaction {

    /**
     * 开启事务
     */
    void begin();

    /**
     * 提交事务
     */
    void commit();

    /**
     * 事务回滚
     */
    void rollBack();
}
