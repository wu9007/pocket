package org.homo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wujianchuan 2018/12/28
 */
@RestController
class HomoDispatchServlet {

    private final ControllerFactory controllerFactory;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public HomoDispatchServlet(ControllerFactory controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    @RequestMapping(
            value = {"/{containerName}/{bundleName}/{executorName:[^.]*}"},
            method = {RequestMethod.POST, RequestMethod.GET}
    )
    @ResponseBody
    void service(@PathVariable String containerName, @PathVariable String bundleName, @PathVariable String executorName, HttpServletRequest request, HttpServletResponse response) {
        HomoExecutor executor = controllerFactory.getExecutor(containerName + "_" + bundleName + "_" + executorName);
        ExecutionResult responseBody = executor.execute(request);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(this.mapper.writeValueAsString(responseBody));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
