package org.hunter.pocket.session.actions;

import org.hunter.pocket.session.Transaction;

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
