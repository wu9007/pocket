package org.hv.pocket.exception;

/**
 * @author wujianchuan
 */
public class PocketIdentifyException extends RuntimeException {

    private static final long serialVersionUID = -669986260310668122L;
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

    public PocketIdentifyException(String errorMsg) {
        super(errorMsg);
    }
}
