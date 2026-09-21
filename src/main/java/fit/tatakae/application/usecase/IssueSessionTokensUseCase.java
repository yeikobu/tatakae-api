package fit.tatakae.application.usecase;

import fit.tatakae.application.port.SessionTokenIssuer;
import fit.tatakae.domain.repository.RefreshTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

public class IssueSessionTokensUseCase {

    private final SessionTokenIssuer sessionTokenIssuer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public IssueSessionTokensUseCase(SessionTokenIssuer sessionTokenIssuer,
                                     RefreshTokenRepository refreshTokenRepository,
                                     Clock clock) {
        this.sessionTokenIssuer = sessionTokenIssuer;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    public SessionTokens execute(String userId) {
        String accessToken = sessionTokenIssuer.createAccessToken(userId);
        String refreshToken = createOpaqueRefreshToken();
        Instant now = clock.instant();
        Instant expiresAt = now.plusSeconds(sessionTokenIssuer.getRefreshTtlSeconds());

        refreshTokenRepository.save(new RefreshTokenRepository.StoredRefreshToken(
                UUID.randomUUID().toString(),
                userId,
                sha256Hex(refreshToken),
                expiresAt,
                null,
                now
        ));

        return new SessionTokens(accessToken, refreshToken, sessionTokenIssuer.getAccessTtlSeconds());
    }

    private String createOpaqueRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
