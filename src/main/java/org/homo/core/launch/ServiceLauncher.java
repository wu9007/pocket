package org.homo.core.launch;

import org.homo.core.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author wujianchuan 2019/1/12
 */
@Component
@Order(value = 2)
public class ServiceLauncher implements CommandLineRunner {
    private final
    Map<String, AbstractService> serviceMap;

    @Autowired
    public ServiceLauncher(Map<String, AbstractService> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    public void run(String... args) throws Exception {
        serviceMap.forEach((key, value) -> value.installTransaction());
    }
}
