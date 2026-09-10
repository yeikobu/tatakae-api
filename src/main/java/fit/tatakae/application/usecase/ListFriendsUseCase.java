package fit.tatakae.application.usecase;

import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

public class ListFriendsUseCase {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public ListFriendsUseCase(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public List<AcceptedFriend> execute(String userId) {
        String identity = UserId.of(userId).asString();
        if (!userRepository.existsById(identity)) {
            throw new ResourceNotFoundException("User " + identity + " was not found");
        }
        return friendshipRepository.findAcceptedFor(identity).stream()
                .map(friendship -> userRepository.findById(friendship.friendOf(identity))
                        .map(friend -> new AcceptedFriend(friendship.getId(), friend)))
                .flatMap(Optional::stream)
                .toList();
    }
}
