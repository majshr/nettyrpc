package com.rpc.exception;

/**
 * 执行错误异常
 * @author maj
 *
 */
public class InvokeErrorException extends RuntimeException {
    public InvokeErrorException() {
        super();
    }

    public InvokeErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokeErrorException(String message) {
        super(message);
    }

    public InvokeErrorException(Throwable cause) {
        super(cause);
    }
}
