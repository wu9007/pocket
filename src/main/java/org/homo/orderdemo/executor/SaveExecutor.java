package org.homo.orderdemo.executor;

import org.homo.core.annotation.Executor;
import org.homo.core.executor.ExecutionResult;
import org.homo.core.executor.HomoExecutor;
import org.homo.core.executor.HomoRequest;
import org.homo.orderdemo.model.Order;
import org.homo.orderdemo.repository.OrderRepositoryImpl;
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
