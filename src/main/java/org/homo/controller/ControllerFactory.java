package org.homo.controller;

import org.homo.common.annotation.Execute;
import org.homo.common.config.HomoConfig;
import org.homo.demo.controller.OrderController;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2018/12/28
 */
@Component
class ControllerFactory {

    private final static Map<String, HomoController> CONTROLLER_POOL = new ConcurrentHashMap<>(500);

    private ControllerFactory(List<HomoController> controllerList, HomoConfig homoConfig) {
        controllerList.forEach(controller -> {
            String className = controller.getClass().getName();
            String packagePath = className.substring(0, className.indexOf("controller") - 1);
            String serverName = homoConfig.getServerName();
            String bundle = packagePath.substring(packagePath.lastIndexOf(".") + 1);
            String controllerName = controller.getClass().getAnnotation(Execute.class).value();
            CONTROLLER_POOL.put("/" + serverName + "/" + bundle + "/" + controllerName, controller);
        });
    }

    HomoController getController(String url) {
        return CONTROLLER_POOL.get(url);
    }

}
