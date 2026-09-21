package fit.tatakae.application.usecase;

import fit.tatakae.domain.exception.InvalidSessionTokenException;
import fit.tatakae.domain.repository.RefreshTokenRepository;

import java.time.Clock;
import java.time.Instant;

public class RefreshSessionTokensUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final IssueSessionTokensUseCase issueSessionTokensUseCase;
    private final Clock clock;

    public RefreshSessionTokensUseCase(RefreshTokenRepository refreshTokenRepository,
                                       IssueSessionTokensUseCase issueSessionTokensUseCase,
                                       Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.issueSessionTokensUseCase = issueSessionTokensUseCase;
        this.clock = clock;
    }

    public SessionTokens execute(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidSessionTokenException("Refresh token is required");
        }

        String hash = IssueSessionTokensUseCase.sha256Hex(refreshToken);
        Instant now = clock.instant();

        RefreshTokenRepository.StoredRefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidSessionTokenException("Invalid or revoked refresh token"));

        if (!stored.isActive(now)) {
            throw new InvalidSessionTokenException("Invalid or expired refresh token");
        }

        refreshTokenRepository.revoke(stored.id(), now);
        return issueSessionTokensUseCase.execute(stored.userId());
    }
}
