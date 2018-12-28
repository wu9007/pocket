package org.homo.controller;

import org.homo.common.constant.CategoryTypes;

/**
 * @author wujianchuan 2018/12/28
 */
public class ExecutionResult {
    private Boolean success;
    private String category;
    private String title;
    private String message;
    private Object body;

    private ExecutionResult(String title, String message, Object body) {
        this.title = title;
        this.message = message;
        this.body = body;
    }

    public static ExecutionResult newSuccessInstance(String title, String message, Object body) {
        ExecutionResult instance = new ExecutionResult(title, message, body);
        instance.setSuccess(true);
        instance.setCategory(CategoryTypes.INFO);
        return instance;
    }

    public static ExecutionResult newFailInstance(String title, String message, Object body) {
        ExecutionResult instance = new ExecutionResult(title, message, body);
        instance.setSuccess(false);
        instance.setCategory(CategoryTypes.WARING);
        return instance;
    }

    public Boolean getSuccess() {
        return success;
    }

    private void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getCategory() {
        return category;
    }

    private void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public Object getBody() {
        return body;
    }

    private void setBody(Object body) {
        this.body = body;
    }
}
