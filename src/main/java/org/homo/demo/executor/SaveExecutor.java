package org.homo.demo.executor;

import org.homo.authority.model.User;
import org.homo.common.annotation.Executor;
import org.homo.controller.ExecutionResult;
import org.homo.controller.HomoExecutor;
import org.homo.demo.model.Order;
import org.homo.demo.repository.OrderRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
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
    public ExecutionResult execute(HttpServletRequest request) {
        Order order = Order.newInstance("A-001", new BigDecimal("12.5"));
        User user = User.newInstance("Homo", "霍姆");
        repository.getProxy().save(order, user);
        return ExecutionResult.newSuccessInstance("成功", "订单保存成功", order);
    }
}
