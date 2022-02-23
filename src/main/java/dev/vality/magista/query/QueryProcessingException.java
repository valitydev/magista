package dev.vality.magista.query;

/**
 * Created by vpankrashkin on 29.08.16.
 */
public class QueryProcessingException extends RuntimeException {
    public QueryProcessingException() {
    }

    public QueryProcessingException(String message) {
        super(message);
    }

    public QueryProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryProcessingException(Throwable cause) {
        super(cause);
    }

    public QueryProcessingException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
