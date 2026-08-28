package fit.tatakae.domain.service;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.FriendshipStatus;
import fit.tatakae.domain.exception.DuplicateFriendshipException;
import fit.tatakae.domain.repository.FriendshipRepository;

import java.time.Clock;
import java.util.Optional;

public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final Clock clock;

    public FriendshipService(FriendshipRepository friendshipRepository, Clock clock) {
        this.friendshipRepository = friendshipRepository;
        this.clock = clock;
    }

    // A rejected relation may be requested again; any other existing relation blocks a new request.
    public Friendship createRequest(String requesterId, String addresseeId) {
        Optional<Friendship> existing = friendshipRepository.findBetween(requesterId, addresseeId);
        if (existing.isPresent() && !isRetryable(existing.get())) {
            throw new DuplicateFriendshipException(
                    "A friendship between " + requesterId + " and " + addresseeId + " already exists with status " + existing.get().getStatus());
        }
        return new Friendship(requesterId, addresseeId, clock);
    }

    private boolean isRetryable(Friendship friendship) {
        return friendship.getStatus() == FriendshipStatus.REJECTED;
    }
}
