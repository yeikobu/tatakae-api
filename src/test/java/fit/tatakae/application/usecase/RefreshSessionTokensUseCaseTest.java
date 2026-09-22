package fit.tatakae.application.usecase;

import fit.tatakae.application.port.SessionTokenIssuer;
import fit.tatakae.domain.exception.InvalidSessionTokenException;
import fit.tatakae.domain.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshSessionTokensUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-21T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private SessionTokenIssuer sessionTokenIssuer;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private IssueSessionTokensUseCase issueUseCase;
    private RefreshSessionTokensUseCase refreshUseCase;

    @BeforeEach
    void setUp() {
        lenient().when(sessionTokenIssuer.getAccessTtlSeconds()).thenReturn(900);
        lenient().when(sessionTokenIssuer.getRefreshTtlSeconds()).thenReturn(5184000);
        lenient().when(sessionTokenIssuer.createAccessToken(anyString())).thenReturn("new-access");
        issueUseCase = new IssueSessionTokensUseCase(sessionTokenIssuer, refreshTokenRepository, CLOCK);
        refreshUseCase = new RefreshSessionTokensUseCase(refreshTokenRepository, issueUseCase, CLOCK);
    }

    @Test
    void shouldRotateRefreshToken() {
        String oldPlain = "old-refresh-token-value";
        String hash = IssueSessionTokensUseCase.sha256Hex(oldPlain);
        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(
                new RefreshTokenRepository.StoredRefreshToken(
                        "rt-1", "user-1", hash,
                        Instant.parse("2026-10-21T12:00:00Z"),
                        null,
                        Instant.parse("2026-09-01T12:00:00Z")
                )
        ));

        SessionTokens tokens = refreshUseCase.execute(oldPlain);

        assertEquals("new-access", tokens.accessToken());
        assertEquals(900, tokens.expiresIn());
        assertNotNull(tokens.refreshToken());
        assertNotEquals(oldPlain, tokens.refreshToken());
        verify(refreshTokenRepository).revoke("rt-1", CLOCK.instant());
        verify(refreshTokenRepository, times(1)).save(any());
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThrows(InvalidSessionTokenException.class, () -> refreshUseCase.execute("missing"));
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        String plain = "expired-token";
        String hash = IssueSessionTokensUseCase.sha256Hex(plain);
        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(
                new RefreshTokenRepository.StoredRefreshToken(
                        "rt-2", "user-1", hash,
                        Instant.parse("2026-09-01T12:00:00Z"),
                        null,
                        Instant.parse("2026-08-01T12:00:00Z")
                )
        ));
        assertThrows(InvalidSessionTokenException.class, () -> refreshUseCase.execute(plain));
        verify(refreshTokenRepository, never()).revoke(anyString(), any());
    }
}
