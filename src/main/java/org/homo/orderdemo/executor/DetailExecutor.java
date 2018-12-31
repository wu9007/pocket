package org.homo.orderdemo.executor;

import org.homo.core.annotation.Executor;
import org.homo.core.executor.ExecutionResult;
import org.homo.core.executor.HomoExecutor;
import org.homo.core.executor.HomoRequest;
import org.homo.orderdemo.service.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wujianchuan 2018/12/28
 */
@Executor(value = "detail")
public class DetailExecutor implements HomoExecutor {
    private final OrderServiceImpl orderService;

    @Autowired
    public DetailExecutor(OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @Override
    public ExecutionResult execute(HomoRequest request) {
        String code = (String) orderService.handle(orderService.getCode, request);
        return ExecutionResult.newSuccessInstance("成功", "请求成功", code);
    }
}
