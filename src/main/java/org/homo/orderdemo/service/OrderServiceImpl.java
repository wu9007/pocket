package org.homo.orderdemo.service;

import org.homo.core.annotation.HomoMessage;
import org.homo.core.annotation.HomoTransaction;
import org.homo.core.service.AbstractService;
import org.homo.core.executor.HomoRequest;
import org.homo.orderdemo.model.Order;
import org.homo.orderdemo.repository.OrderRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @HomoTransaction(sessionName = "demo")
    @HomoMessage(type = Order.class)
    public BiFunction<HomoRequest, OrderRepositoryImpl, Object> getCode = (request, repository) -> "A-001";

    @HomoTransaction(sessionName = "demo")
    @HomoMessage(type = Order.class)
    public BiFunction<HomoRequest, OrderRepositoryImpl, Object> discount = (request, repository) -> {
        String uuid = request.getParameter("uuid");
        Order order = repository.findOne(uuid);
        order.setPrice(order.getPrice().add(new BigDecimal("1")));
        repository.getProxy().update(order, request.getUser());
        return order.getPrice().toString();
    };
}
