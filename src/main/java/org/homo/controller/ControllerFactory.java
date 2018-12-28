package org.homo.controller;

import org.homo.common.annotation.Executor;
import org.homo.common.config.HomoConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/28
 */
@Component
class ControllerFactory {

    private final static Map<String, HomoExecutor> CONTROLLER_POOL = new ConcurrentHashMap<>(500);

    private ControllerFactory(List<HomoExecutor> controllerList, HomoConfig homoConfig) {
        controllerList.forEach(controller -> {
            String className = controller.getClass().getName();
            String packagePath = className.substring(0, className.indexOf("executor") - 1);
            String serverName = homoConfig.getServerName();
            String bundle = packagePath.substring(packagePath.lastIndexOf(".") + 1);
            String executorName = controller.getClass().getAnnotation(Executor.class).value();
            CONTROLLER_POOL.put(serverName + "_" + bundle + "_" + executorName, controller);
        });
    }

    HomoExecutor getExecutor(String url) {
        return CONTROLLER_POOL.get(url);
    }

}
