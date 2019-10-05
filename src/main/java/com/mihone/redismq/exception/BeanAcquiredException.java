package com.mihone.redismq.exception;

public class BeanAcquiredException extends BaseException {
    public BeanAcquiredException() {
    }

    public BeanAcquiredException(String message) {
        super(message);
    }

    public BeanAcquiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanAcquiredException(Throwable cause) {
        super(cause);
    }

    public BeanAcquiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
