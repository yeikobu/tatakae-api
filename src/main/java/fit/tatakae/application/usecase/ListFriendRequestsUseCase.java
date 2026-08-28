package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.UserId;

import java.util.List;

public class ListFriendRequestsUseCase {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public ListFriendRequestsUseCase(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public List<Friendship> execute(String userId, FriendRequestDirection direction) {
        String identity = UserId.of(userId).asString();
        if (!userRepository.existsById(identity)) {
            throw new ResourceNotFoundException("User " + identity + " was not found");
        }
        return direction == FriendRequestDirection.INCOMING
                ? friendshipRepository.findPendingIncoming(identity)
                : friendshipRepository.findPendingOutgoing(identity);
    }
}
