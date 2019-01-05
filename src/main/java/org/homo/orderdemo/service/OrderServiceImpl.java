package org.homo.orderdemo.service;

import org.homo.core.annotation.Message;
import org.homo.core.annotation.Transaction;
import org.homo.core.service.AbstractService;
import org.homo.core.executor.HomoRequest;
import org.homo.orderdemo.model.Order;
import org.homo.orderdemo.repository.OrderRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.function.BiFunction;

/**
 * @author wujianchuan 2018/12/29
 */
@Service
public class OrderServiceImpl extends AbstractService<OrderRepositoryImpl> {
    @Autowired
    public OrderServiceImpl(OrderRepositoryImpl repository) {
        super(repository);
    }

    @Transaction(sessionName = "demo")
    @Message(type = Order.class)
    public BiFunction<HomoRequest, OrderRepositoryImpl, Object> getCode = (request, repository) -> "A-001";

    @Transaction(sessionName = "demo")
    @Message(type = Order.class)
    public BiFunction<HomoRequest, OrderRepositoryImpl, Object> discount = (request, repository) -> {
        Order order;
        try {
            long uuid = Long.parseLong(request.getParameter("uuid"));
            order = repository.findOne(uuid);
            order.setPrice(order.getPrice().add(new BigDecimal("1")));
            repository.getProxy().update(order, request.getUser());
            return order.getPrice().toString();
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    };
}
