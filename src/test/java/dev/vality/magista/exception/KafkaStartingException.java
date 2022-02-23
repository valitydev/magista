package dev.vality.magista.exception;

public class KafkaStartingException extends RuntimeException {

    public KafkaStartingException(String message, Throwable cause) {
        super(message, cause);
    }
}
