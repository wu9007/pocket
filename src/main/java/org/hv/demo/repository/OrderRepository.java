package org.hv.demo.repository;

import org.hv.demo.model.Order;

import java.util.List;

/**
 * @author wujianchuan
 */
public interface OrderRepository {
    /**
     * load order by code.
     *
     * @param code order code.
     * @return order.
     */
    List<Order> loadByCode(String code);
}
