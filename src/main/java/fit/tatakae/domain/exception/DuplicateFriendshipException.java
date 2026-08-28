package fit.tatakae.domain.exception;

public class DuplicateFriendshipException extends RuntimeException {
    public DuplicateFriendshipException(String message) {
        super(message);
    }
}
