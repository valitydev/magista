package dev.vality.magista.query;

/**
 * Created by vpankrashkin on 09.08.16.
 */
public class QueryExecutionException extends QueryProcessingException {
    public QueryExecutionException() {
    }

    public QueryExecutionException(String message) {
        super(message);
    }

    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryExecutionException(Throwable cause) {
        super(cause);
    }

    public QueryExecutionException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
