package fit.tatakae.domain.exception;

public class FraudulentSessionException extends RuntimeException {
    public FraudulentSessionException(String message) {
        super(message);
    }
}
