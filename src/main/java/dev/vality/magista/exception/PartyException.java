package dev.vality.magista.exception;

/**
 * Created by tolkonepiu on 29/05/2017.
 */
public class PartyException extends RuntimeException {

    public PartyException(String message) {
        super(message);
    }

    public PartyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PartyException(Throwable cause) {
        super(cause);
    }

    public PartyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
