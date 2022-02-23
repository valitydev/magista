package dev.vality.magista.exception;

public class TokenGeneratorException extends RuntimeException {

    public TokenGeneratorException() {
        super();
    }

    public TokenGeneratorException(String message) {
        super(message);
    }

    public TokenGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenGeneratorException(Throwable cause) {
        super(cause);
    }
}
