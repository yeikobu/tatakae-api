package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;

public class RespondFriendRequestUseCase {
    private final FriendshipRepository friendshipRepository;

    public RespondFriendRequestUseCase(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    public Friendship accept(String friendshipId) {
        Friendship friendship = require(friendshipId);
        friendship.accept();
        return friendshipRepository.save(friendship);
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
