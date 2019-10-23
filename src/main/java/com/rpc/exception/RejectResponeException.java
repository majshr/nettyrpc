package com.rpc.exception;

/**
 * 拒绝响应异常
 * @author maj
 *
 */
public class RejectResponeException extends RuntimeException {
    public RejectResponeException() {
        super();
    }

    public RejectResponeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RejectResponeException(String message) {
        super(message);
    }

    public RejectResponeException(Throwable cause) {
        super(cause);
    }
}