package org.homo.controller;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wujianchuan 2018/12/28
 */
public interface HomoExecutor {
    /**
     * 处理请求
     *
     * @return 响应结果集
     */
    ExecutionResult execute(HomoRequest request);
}
