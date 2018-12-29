package org.homo.demo.repository;

import org.homo.demo.model.Order;

/**
 * @author wujianchuan 2018/12/29
 */
public interface OrderRepository {
    /**
     * 获取订单
     *
     * @param uuid 数据标识
     * @return 订单
     */
    Order findOne(String uuid);
}
