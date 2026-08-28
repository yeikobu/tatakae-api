package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.service.FriendshipService;
import fit.tatakae.domain.valueobject.UserId;

public class SendFriendRequestUseCase {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipService friendshipService;

    public SendFriendRequestUseCase(UserRepository userRepository,
                                    FriendshipRepository friendshipRepository,
                                    FriendshipService friendshipService) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendshipService = friendshipService;
    }

    public Friendship execute(String requesterId, String addresseeId) {
        String requester = requireUser(requesterId);
        String addressee = requireUser(addresseeId);
        return friendshipRepository.save(friendshipService.createRequest(requester, addressee));
    }

    private String requireUser(String userId) {
        String identity = UserId.of(userId).asString();
        if (!userRepository.existsById(identity)) {
            throw new ResourceNotFoundException("User " + identity + " was not found");
        }
        return identity;
    }
}
