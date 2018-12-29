package org.homo.demo.executor;

import org.homo.common.annotation.Executor;
import org.homo.controller.ExecutionResult;
import org.homo.controller.HomoExecutor;
import org.homo.controller.HomoRequest;
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
