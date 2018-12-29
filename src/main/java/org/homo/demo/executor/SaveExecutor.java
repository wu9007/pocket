package org.homo.demo.executor;

import org.homo.authority.model.User;
import org.homo.common.annotation.Executor;
import org.homo.controller.ExecutionResult;
import org.homo.controller.HomoExecutor;
import org.homo.controller.HomoRequest;
import org.homo.demo.model.Order;
import org.homo.demo.repository.OrderRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * @author wujianchuan 2018/12/28
 */
@Executor(value = "save")
public class SaveExecutor implements HomoExecutor {
    private final
    OrderRepositoryImpl repository;

    @Autowired
    public SaveExecutor(OrderRepositoryImpl repository) {
        this.repository = repository;
    }

    @Override
    public ExecutionResult execute(HomoRequest request) {
        Order order = Order.newInstance("A-001", new BigDecimal("12.5"));
        repository.getProxy().save(order, request.getUser());
        return ExecutionResult.newSuccessInstance("成功", "订单保存成功", order);
    }
}
