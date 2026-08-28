package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;

public class RemoveFriendshipUseCase {
    private final FriendshipRepository friendshipRepository;

    public RemoveFriendshipUseCase(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    public void execute(String friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship " + friendshipId + " was not found"));
        friendshipRepository.delete(friendship.getId());
    }
}
