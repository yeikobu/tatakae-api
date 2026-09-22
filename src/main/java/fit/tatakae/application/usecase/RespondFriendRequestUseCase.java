package fit.tatakae.application.usecase;

import fit.tatakae.application.event.FriendRequestAcceptedEvent;
import fit.tatakae.application.port.UserEventPublisher;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.UserRepository;

public class RespondFriendRequestUseCase {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    public RespondFriendRequestUseCase(FriendshipRepository friendshipRepository,
                                       UserRepository userRepository,
                                       UserEventPublisher userEventPublisher) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
    }

    public Friendship accept(String friendshipId) {
        Friendship friendship = require(friendshipId);
        friendship.accept();
        Friendship saved = friendshipRepository.save(friendship);

        User acceptedBy = userRepository.findById(saved.getAddresseeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User " + saved.getAddresseeId() + " was not found"));
        userEventPublisher.publishFriendRequestAccepted(
                saved.getRequesterId(),
                new FriendRequestAcceptedEvent(saved, acceptedBy));

        return saved;
    }

    public Friendship reject(String friendshipId) {
        Friendship friendship = require(friendshipId);
        friendship.reject();
        return friendshipRepository.save(friendship);
    }

    private Friendship require(String friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship " + friendshipId + " was not found"));
    }
}
