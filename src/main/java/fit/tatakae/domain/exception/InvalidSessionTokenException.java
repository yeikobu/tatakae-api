package fit.tatakae.domain.exception;

public class InvalidSessionTokenException extends RuntimeException {

    public InvalidSessionTokenException(String message) {
        super(message);
    }

    public InvalidSessionTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
