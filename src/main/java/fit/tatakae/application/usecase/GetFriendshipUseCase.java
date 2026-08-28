package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;

public class GetFriendshipUseCase {
    private final FriendshipRepository friendshipRepository;

    public GetFriendshipUseCase(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    public Friendship execute(String friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship " + friendshipId + " was not found"));
    }
}
