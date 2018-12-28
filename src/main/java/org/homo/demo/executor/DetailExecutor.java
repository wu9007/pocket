package org.homo.demo.executor;

import org.homo.common.annotation.Executor;
import org.homo.controller.ExecutionResult;
import org.homo.controller.HomoExecutor;
import org.homo.demo.model.Order;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * @author wujianchuan 2018/12/28
 */
@Executor(value = "detail")
public class DetailExecutor implements HomoExecutor {
    @Override
    public ExecutionResult execute(HttpServletRequest request) {
        Order order = Order.newInstance("A-001", new BigDecimal(100.5));
        order.setUuid("123");
        return ExecutionResult.newSuccessInstance("成功", "请求成功", order);
    }
}
