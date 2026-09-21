package fit.tatakae.infrastructure.web.security.jwt;

public class InvalidSessionJwtException extends RuntimeException {

    public InvalidSessionJwtException(String message) {
        super(message);
    }

    public InvalidSessionJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
