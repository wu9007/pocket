package org.hunter.pocket.exception;

/**
 * @author wujianchuan
 */
public class CriteriaException extends RuntimeException {
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

    public CriteriaException(String errorMsg) {
        super(errorMsg);
    }
}
