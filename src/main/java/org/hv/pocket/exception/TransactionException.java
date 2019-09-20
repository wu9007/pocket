package org.hv.pocket.exception;

/**
 * @author wujianchuan
 */
public class TransactionException extends RuntimeException {
    private static final long serialVersionUID = 3959959252275580404L;
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

    public TransactionException(String errorMsg) {
        super(errorMsg);
    }
}
