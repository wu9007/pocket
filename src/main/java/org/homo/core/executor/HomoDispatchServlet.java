package org.homo.core.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.homo.authority.model.User;
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
            value = {"/{bundleName}/{executorName:[^.]*}"},
            method = {RequestMethod.POST, RequestMethod.GET}
    )
    @ResponseBody
    void service(@PathVariable String bundleName, @PathVariable String executorName, HttpServletRequest request, HttpServletResponse response) {
        User user = User.newInstance("Home", "霍姆");
        HomoRequest homoRequest = HomoRequest.newInstance(request, user);
        String contextPath = request.getContextPath();
        String url = (contextPath.length() > 1 ? contextPath.substring(1) : contextPath)
                + "_" + bundleName
                + "_" + executorName;
        HomoExecutor executor = controllerFactory.getExecutor(url);
        ExecutionResult responseBody;
        if (executor != null) {
            responseBody = executor.execute(homoRequest);
        } else {
            throw new NullPointerException("未找到执行器【" + url + "】");
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(this.mapper.writeValueAsString(responseBody));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
