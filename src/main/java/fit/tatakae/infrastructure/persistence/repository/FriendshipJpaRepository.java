package fit.tatakae.infrastructure.persistence.repository;

import fit.tatakae.domain.entity.FriendshipStatus;
import fit.tatakae.infrastructure.persistence.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipJpaRepository extends JpaRepository<FriendshipEntity, String> {

    // A rejected relation can be requested again, so the newest row is the one that rules.
    Optional<FriendshipEntity> findFirstByRequesterIdAndAddresseeIdOrderByCreatedAtDesc(String requesterId, String addresseeId);

    List<FriendshipEntity> findByStatusAndRequesterId(FriendshipStatus status, String requesterId);

    List<FriendshipEntity> findByStatusAndAddresseeId(FriendshipStatus status, String addresseeId);

    List<FriendshipEntity> findByStatusAndRequesterIdOrStatusAndAddresseeId(
            FriendshipStatus requesterStatus, String requesterId,
            FriendshipStatus addresseeStatus, String addresseeId);
}
