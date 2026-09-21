package fit.tatakae.infrastructure.persistence.repository;

import fit.tatakae.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshTokenEntity r SET r.revokedAt = :revokedAt WHERE r.userId = :userId AND r.revokedAt IS NULL")
    int revokeAllActiveForUser(@Param("userId") String userId, @Param("revokedAt") Instant revokedAt);
}
