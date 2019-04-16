package org.hunter.demo.repository;

import org.hunter.demo.model.Order;

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
