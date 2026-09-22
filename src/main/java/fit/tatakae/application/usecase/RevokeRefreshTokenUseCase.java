package fit.tatakae.application.usecase;

import fit.tatakae.domain.exception.InvalidSessionTokenException;
import fit.tatakae.domain.repository.RefreshTokenRepository;

import java.time.Clock;
import java.time.Instant;

public class RevokeRefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public RevokeRefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    /**
     * Revokes the given refresh token if it belongs to {@code userId}.
     * Idempotent for already-revoked tokens that match the user.
     */
    public void execute(String userId, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidSessionTokenException("Refresh token is required");
        }

        String hash = IssueSessionTokensUseCase.sha256Hex(refreshToken);
        Instant now = clock.instant();

        RefreshTokenRepository.StoredRefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidSessionTokenException("Invalid refresh token"));

        if (!stored.userId().equals(userId)) {
            throw new InvalidSessionTokenException("Invalid refresh token");
        }

        if (stored.revokedAt() == null) {
            refreshTokenRepository.revoke(stored.id(), now);
        }
    }
}
