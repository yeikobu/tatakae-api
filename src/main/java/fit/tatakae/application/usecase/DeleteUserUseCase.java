package fit.tatakae.application.usecase;

import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.SessionRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.UserId;

public class DeleteUserUseCase {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final SessionRepository sessionRepository;

    public DeleteUserUseCase(UserRepository userRepository,
                             FriendshipRepository friendshipRepository,
                             SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.sessionRepository = sessionRepository;
    }

    // Everything that points at the athlete goes with it: leaving orphan rows behind would either
    // break referential integrity or resurrect the athlete inside somebody else's ranking.
    public void execute(String userId) {
        String identity = UserId.of(userId).asString();
        if (!userRepository.existsById(identity)) {
            throw new ResourceNotFoundException("User " + identity + " was not found");
        }
        sessionRepository.deleteAllOf(identity);
        friendshipRepository.deleteAllInvolving(identity);
        userRepository.delete(identity);
    }
}
