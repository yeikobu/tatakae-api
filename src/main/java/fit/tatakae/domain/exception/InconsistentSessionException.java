package fit.tatakae.domain.exception;

public class InconsistentSessionException extends RuntimeException {
    public InconsistentSessionException(String message) {
        super(message);
    }
}
