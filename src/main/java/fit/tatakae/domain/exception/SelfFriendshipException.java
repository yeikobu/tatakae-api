package fit.tatakae.domain.exception;

public class SelfFriendshipException extends RuntimeException {
    public SelfFriendshipException(String message) {
        super(message);
    }
}
