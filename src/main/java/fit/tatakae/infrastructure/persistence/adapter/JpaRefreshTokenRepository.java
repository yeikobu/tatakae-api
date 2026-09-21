package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.domain.repository.RefreshTokenRepository;
import fit.tatakae.infrastructure.persistence.entity.RefreshTokenEntity;
import fit.tatakae.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public JpaRefreshTokenRepository(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(StoredRefreshToken token) {
        jpaRepository.save(new RefreshTokenEntity(
                token.id(),
                token.userId(),
                token.tokenHash(),
                token.expiresAt(),
                token.revokedAt(),
                token.createdAt()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredRefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revoke(String id, Instant revokedAt) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setRevokedAt(revokedAt);
            jpaRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void revokeAllForUser(String userId, Instant revokedAt) {
        jpaRepository.revokeAllActiveForUser(userId, revokedAt);
    }

    private StoredRefreshToken toDomain(RefreshTokenEntity entity) {
        return new StoredRefreshToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getCreatedAt()
        );
    }
}
