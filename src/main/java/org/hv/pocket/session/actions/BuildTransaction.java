package org.hv.pocket.session.actions;

import org.hv.pocket.session.Transaction;

/**
 * @author wujianchuan
 */
public interface BuildTransaction {

    /**
     * 获取事务对象
     *
     * @return 事务对象
     */
    Transaction getTransaction();
}
