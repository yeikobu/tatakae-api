package fit.tatakae.infrastructure.web.security.jwt;

public class InvalidAppleJwtException extends RuntimeException {

    public InvalidAppleJwtException(String message) {
        super(message);
    }

    public InvalidAppleJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
