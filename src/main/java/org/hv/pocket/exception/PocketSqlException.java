package org.hv.pocket.exception;

/**
 * @author wujianchuan
 */
public class PocketSqlException extends RuntimeException {

    private static final long serialVersionUID = 3461708034111021888L;
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

    public PocketSqlException(Throwable cause) {
        super(cause);
    }

    public PocketSqlException(String errorMsg) {
        super(errorMsg);
    }

    public PocketSqlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
