package org.homo.demo.service;

import org.homo.common.annotation.HomoMessage;
import org.homo.common.annotation.HomoTransaction;
import org.homo.common.service.AbstractService;
import org.homo.demo.model.Order;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

/**
 * @author wujianchuan 2018/12/29
 */
@Service
public class OrderServiceImpl extends AbstractService {

    @HomoTransaction(sessionName = "demo")
    @HomoMessage(open = true)
    public Function<Map<String, Object>, Object> getCode = (parameter) -> ((Order) parameter.get("order")).getCode();
}
