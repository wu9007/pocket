package org.homo.demo.service;

import org.homo.common.annotation.HomoMessage;
import org.homo.common.annotation.HomoTransaction;
import org.homo.common.service.AbstractService;
import org.homo.controller.HomoRequest;
import org.homo.demo.model.Order;
import org.homo.demo.repository.OrderRepositoryImpl;
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
    @HomoMessage(open = true)
    public BiFunction<HomoRequest, OrderRepositoryImpl, Object> getCode = (request, repository) -> "A-001";

    @HomoTransaction(sessionName = "demo")
    @HomoMessage(open = true)
    public BiFunction<HomoRequest, OrderRepositoryImpl, Object> discount = (request, repository) -> {
        String uuid = request.getParameter("uuid");
        Order order = repository.findOne(uuid);
        order.setPrice(order.getPrice().multiply(new BigDecimal("0.85")));
        repository.getProxy().update(order, request.getUser());
        return order.getPrice().toString();
    };
}
