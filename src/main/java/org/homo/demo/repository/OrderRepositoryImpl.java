package org.homo.demo.repository;

import org.homo.authority.model.User;
import org.homo.demo.model.Order;
import org.homo.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * @author wujianchuan 2018/12/26
 */
@Repository
public class OrderRepositoryImpl extends AbstractRepository<Order> implements OrderRepository {

    @Override
    public int save(Order entity, User operator) {
        System.out.println("保存订单。");
        return 1;
    }

    @Override
    public int update(Order entity, User operator) {
        System.out.println("更新订单。");
        return 1;
    }

    @Override
    public int delete(Order entity, User operator) {
        System.out.println("删除订单。");
        return 1;
    }


    @Override
    public Order findOne(String uuid) {
        return Order.newInstance("B-001", new BigDecimal("100.54"));
    }
}
