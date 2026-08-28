package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.FriendshipStatus;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.infrastructure.persistence.entity.FriendshipEntity;
import fit.tatakae.infrastructure.persistence.mapper.FriendshipMapper;
import fit.tatakae.infrastructure.persistence.repository.FriendshipJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class JpaFriendshipRepository implements FriendshipRepository {
    private final FriendshipJpaRepository friendshipJpaRepository;
    private final Clock clock;

    public JpaFriendshipRepository(FriendshipJpaRepository friendshipJpaRepository, Clock clock) {
        this.friendshipJpaRepository = friendshipJpaRepository;
        this.clock = clock;
    }

    @Override
    public Optional<Friendship> findById(String id) {
        return friendshipJpaRepository.findById(id).map(this::toDomain);
    }

    // The relation is direction agnostic: the newest row between both users is the one that counts.
    @Override
    public Optional<Friendship> findBetween(String userId, String otherUserId) {
        Optional<FriendshipEntity> outgoing =
                friendshipJpaRepository.findFirstByRequesterIdAndAddresseeIdOrderByCreatedAtDesc(userId, otherUserId);
        Optional<FriendshipEntity> incoming =
                friendshipJpaRepository.findFirstByRequesterIdAndAddresseeIdOrderByCreatedAtDesc(otherUserId, userId);

        return Stream.concat(outgoing.stream(), incoming.stream())
                .max(Comparator.comparing(FriendshipEntity::getCreatedAt))
                .map(this::toDomain);
    }

    @Override
    public List<Friendship> findAcceptedFor(String userId) {
        return friendshipJpaRepository
                .findByStatusAndRequesterIdOrStatusAndAddresseeId(
                        FriendshipStatus.ACCEPTED, userId, FriendshipStatus.ACCEPTED, userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findPendingIncoming(String userId) {
        return friendshipJpaRepository.findByStatusAndAddresseeId(FriendshipStatus.PENDING, userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findPendingOutgoing(String userId) {
        return friendshipJpaRepository.findByStatusAndRequesterId(FriendshipStatus.PENDING, userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Friendship save(Friendship friendship) {
        return toDomain(friendshipJpaRepository.save(FriendshipMapper.toEntity(friendship)));
    }

    @Override
    public void delete(String id) {
        friendshipJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllInvolving(String userId) {
        friendshipJpaRepository.deleteByRequesterIdOrAddresseeId(userId, userId);
    }

    private Friendship toDomain(FriendshipEntity entity) {
        return FriendshipMapper.toDomain(entity, clock);
    }
}
