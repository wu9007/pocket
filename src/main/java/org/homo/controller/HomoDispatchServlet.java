package org.homo.controller;

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
public class HomoDispatchServlet {

    @RequestMapping(
            value = {"/{containerName}/{bundleName}/{actionName:[^.]*}"},
            method = {RequestMethod.POST, RequestMethod.GET}
    )
    @ResponseBody
    void service(@PathVariable String containerName, @PathVariable String bundleName, @PathVariable String actionName, HttpServletRequest request, HttpServletResponse response) {
        try {
            response.getWriter().write("hello world.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
