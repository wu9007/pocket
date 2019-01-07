package org.homo.orderdemo.executor;

import org.homo.core.annotation.Executor;
import org.homo.core.executor.ExecutionResult;
import org.homo.core.executor.HomoExecutor;
import org.homo.core.executor.HomoRequest;
import org.homo.orderdemo.model.Order;
import org.homo.orderdemo.service.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wujianchuan 2018/12/28
 */
@Executor(value = "save")
public class SaveExecutor implements HomoExecutor {
    final
    private OrderServiceImpl orderService;

    @Autowired
    public SaveExecutor(OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @Override
    public ExecutionResult execute(HomoRequest request) throws Exception {
        Order order = (Order) orderService.handle(orderService.saveOrder, request);
        return ExecutionResult.newSuccessInstance("成功", "订单保存成功", order);
    }
}
