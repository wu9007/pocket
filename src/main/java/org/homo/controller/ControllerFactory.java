package org.homo.controller;

import org.homo.common.annotation.Executor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/28
 */
@Component
class ControllerFactory {

    private final static Map<String, HomoExecutor> CONTROLLER_POOL = new ConcurrentHashMap<>(500);

    private ControllerFactory(List<HomoExecutor> controllerList, WebApplicationContext context) {
        controllerList.forEach(controller -> {
            String className = controller.getClass().getName();
            String packagePath = className.substring(0, className.indexOf("executor") - 1);
            String serverName = Objects.requireNonNull(context.getEnvironment().getProperty("server.servlet.context-path")).substring(1);
            String bundle = packagePath.substring(packagePath.lastIndexOf(".") + 1);
            String executorName = controller.getClass().getAnnotation(Executor.class).value();
            CONTROLLER_POOL.put(serverName + "_" + bundle + "_" + executorName, controller);
        });
    }

    HomoExecutor getExecutor(String url) {
        return CONTROLLER_POOL.get(url);
    }

}
