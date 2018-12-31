package org.homo.demo.executor;

import org.homo.core.annotation.Executor;
import org.homo.core.executor.ExecutionResult;
import org.homo.core.executor.HomoExecutor;
import org.homo.core.executor.HomoRequest;
import org.homo.demo.service.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wujianchuan 2018/12/29
 */
@Executor(value = "discount")
public class DiscountExecutor implements HomoExecutor {
    final
    private OrderServiceImpl orderService;

    @Autowired
    public DiscountExecutor(OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @Override
    public ExecutionResult execute(HomoRequest request) {
        String price = (String) orderService.handle(orderService.discount, request);
        return ExecutionResult.newSuccessInstance("成功", "打折成功", "VIP-" + price);
    }
}
