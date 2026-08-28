package fit.tatakae.domain.repository;

import fit.tatakae.domain.entity.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {
    Optional<Friendship> findById(String id);
    Optional<Friendship> findBetween(String userId, String otherUserId);
    List<Friendship> findAcceptedFor(String userId);
    List<Friendship> findPendingIncoming(String userId);
    List<Friendship> findPendingOutgoing(String userId);
    Friendship save(Friendship friendship);
    void delete(String id);
}
