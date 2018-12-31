package org.homo.orderdemo;

import org.homo.listeners.AbstractSender;
import org.homo.orderdemo.model.Order;
import org.springframework.stereotype.Component;

/**
 * @author wujianchuan 2018/12/31
 */
@Component
public class OrderMessageSender extends AbstractSender {
    @Override
    public Class supportsType() {
        return Order.class;
    }

    @Override
    public void send(Object object) {
        System.out.println("发送短息：" + object);
    }
}
