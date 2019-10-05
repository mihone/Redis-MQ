package com.mihone.redismq.exception;

public class BaseException extends RuntimeException {
    BaseException() {
    }

    BaseException(String message) {
        super(message);
    }

    BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    BaseException(Throwable cause) {
        super(cause);
    }

    BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getStackMessage() {
        StringBuilder sb = new StringBuilder(this.toString());
        sb.append(this.getMessage()).append("\n");
        StackTraceElement[] stackTrace = this.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            sb.append(" ").append(element).append("\n");
        }
        return sb.toString();
    }
}
