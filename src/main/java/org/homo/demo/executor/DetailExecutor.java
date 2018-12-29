package org.homo.demo.executor;

import org.homo.common.annotation.Executor;
import org.homo.controller.ExecutionResult;
import org.homo.controller.HomoExecutor;
import org.homo.demo.model.Order;
import org.homo.demo.service.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
    public ExecutionResult execute(HttpServletRequest request) {
        Map<String, Object> parameters = new HashMap<>();
        Order order = Order.newInstance("A-001", new BigDecimal(100.5));
        parameters.put("order", order);
        String code = (String) orderService.handle(orderService.getCode, parameters);
        return ExecutionResult.newSuccessInstance("成功", "请求成功", code);
    }
}
