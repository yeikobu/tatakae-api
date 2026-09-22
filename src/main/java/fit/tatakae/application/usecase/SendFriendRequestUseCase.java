package fit.tatakae.application.usecase;

import fit.tatakae.application.event.FriendRequestReceivedEvent;
import fit.tatakae.application.port.UserEventPublisher;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.service.FriendshipService;
import fit.tatakae.domain.valueobject.UserId;

public class SendFriendRequestUseCase {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipService friendshipService;
    private final UserEventPublisher userEventPublisher;

    public SendFriendRequestUseCase(UserRepository userRepository,
                                    FriendshipRepository friendshipRepository,
                                    FriendshipService friendshipService,
                                    UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendshipService = friendshipService;
        this.userEventPublisher = userEventPublisher;
    }

    public Friendship execute(String requesterId, String addresseeId) {
        String requester = requireUser(requesterId);
        String addressee = requireUser(addresseeId);
        Friendship saved = friendshipRepository.save(friendshipService.createRequest(requester, addressee));

        User fromUser = userRepository.findById(requester)
                .orElseThrow(() -> new ResourceNotFoundException("User " + requester + " was not found"));
        userEventPublisher.publishFriendRequestReceived(
                addressee,
                new FriendRequestReceivedEvent(saved, fromUser));

        return saved;
    }

    private String requireUser(String userId) {
        String identity = UserId.of(userId).asString();
        if (!userRepository.existsById(identity)) {
            throw new ResourceNotFoundException("User " + identity + " was not found");
        }
        return identity;
    }
}
