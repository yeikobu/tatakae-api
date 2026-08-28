package fit.tatakae.domain.exception;

public class InvalidFriendshipTransitionException extends RuntimeException {
    public InvalidFriendshipTransitionException(String message) {
        super(message);
    }
}
