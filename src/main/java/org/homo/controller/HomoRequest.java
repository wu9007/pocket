package org.homo.controller;

import org.homo.authority.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/29
 */
public class HomoRequest {

    private HttpServletRequest httpServletRequest;

    private User user;

    private HomoRequest() {
    }

    public String getParameter(String name) {
        return this.httpServletRequest.getParameter(name);
    }

    public Map<String, String[]> getParameterMap() {
        return this.httpServletRequest.getParameterMap();
    }

    public User getUser() {
        return user;
    }

    static HomoRequest newInstance(HttpServletRequest request, User user) {
        HomoRequest homoRequest = new HomoRequest();
        homoRequest.setHttpServletRequest(request);
        homoRequest.setUser(user);
        return homoRequest;
    }

    private HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    private void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    private void setUser(User user) {
        this.user = user;
    }
}
