package dev.vality.magista.exception;

/**
 * Created by tolkonepiu on 22.08.16.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    protected NotFoundException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
