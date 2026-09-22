package fit.tatakae.domain.repository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

    record StoredRefreshToken(
            String id,
            String userId,
            String tokenHash,
            Instant expiresAt,
            Instant revokedAt,
            Instant createdAt
    ) {
        public boolean isActive(Instant now) {
            return revokedAt == null && expiresAt.isAfter(now);
        }
    }

    void save(StoredRefreshToken token);

    Optional<StoredRefreshToken> findByTokenHash(String tokenHash);

    void revoke(String id, Instant revokedAt);

    void revokeAllForUser(String userId, Instant revokedAt);
}
