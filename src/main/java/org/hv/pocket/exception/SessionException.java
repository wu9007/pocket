package org.hv.pocket.exception;

/**
 * @author wujianchuan
 */
public class SessionException extends RuntimeException {
    private static final long serialVersionUID = 7010927836528837168L;
    private String errorMsg;

    private String code;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SessionException(String errorMsg) {
        super(errorMsg);
    }

    public SessionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
