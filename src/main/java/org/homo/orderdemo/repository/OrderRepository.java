package org.homo.orderdemo.repository;

import org.homo.orderdemo.model.Order;

import java.sql.SQLException;

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
    Order findOne(long uuid) throws Exception;
}
