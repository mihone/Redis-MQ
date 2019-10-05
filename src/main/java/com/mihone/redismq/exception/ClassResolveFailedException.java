package com.mihone.redismq.exception;

public class ClassResolveFailedException extends BaseException {

    public ClassResolveFailedException() {
    }

    public ClassResolveFailedException(String message) {
        super(message);
    }

    public ClassResolveFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassResolveFailedException(Throwable cause) {
        super(cause);
    }

    public ClassResolveFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
