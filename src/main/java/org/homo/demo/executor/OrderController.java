package org.homo.demo.executor;

import org.homo.common.annotation.Executor;
import org.homo.controller.HomoExecutor;

/**
 * @author wujianchuan 2018/12/28
 */
@Executor(value = "list")
public class OrderController implements HomoExecutor {
    @Override
    public void execute() {
        System.out.println("executed");
    }
}
