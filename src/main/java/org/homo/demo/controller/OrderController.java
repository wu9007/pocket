package org.homo.demo.controller;

import org.homo.common.annotation.Execute;

/**
 * @author wujianchuan 2018/12/28
 */
@Execute(value = "list")
public class OrderController implements org.homo.controller.HomoController {
    @Override
    public void execute() {
        System.out.println("executed");
    }
}
