package dev.vality.magista.exception;

public class ConsumerException extends RuntimeException {

    public ConsumerException() {
    }

    public ConsumerException(String message) {
        super(message);
    }

    public ConsumerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsumerException(Throwable cause) {
        super(cause);
    }

    public ConsumerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
