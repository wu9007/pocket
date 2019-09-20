package org.hv.pocket.exception;

/**
 * @author wujianchuan
 */
public class CriteriaException extends RuntimeException {
    private static final long serialVersionUID = -8252587819436358640L;
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
