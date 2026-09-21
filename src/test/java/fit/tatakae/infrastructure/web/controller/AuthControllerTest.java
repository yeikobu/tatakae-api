package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.application.usecase.GetUserUseCase;
import fit.tatakae.application.usecase.SessionTokens;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.dto.AppleSignInRequest;
import fit.tatakae.infrastructure.web.dto.AppleSignInResponse;
import fit.tatakae.infrastructure.web.dto.RefreshTokenRequest;
import fit.tatakae.infrastructure.web.dto.SessionTokenResponse;
import fit.tatakae.infrastructure.web.security.SessionAuthService;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtClaims;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtValidator;
import fit.tatakae.infrastructure.web.security.jwt.InvalidAppleJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AppleJwtValidator appleJwtValidator;

    @Mock
    private FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private SessionAuthService sessionAuthService;

    private AuthController controller;

    @BeforeEach
    public void setUp() {
        controller = new AuthController(
                appleJwtValidator,
                findOrCreateUserByAppleSubUseCase,
                getUserUseCase,
                sessionAuthService
        );
    }

    private SessionTokens sampleTokens() {
        return new SessionTokens("access.jwt", "opaque-refresh", 900);
    }

    @Test
    public void shouldCreateNewUserOnFirstSignIn() {
        String identityToken = "valid.apple.jwt";
        String appleSub = "001234.567890abcdef.1234";
        AppleSignInRequest request = new AppleSignInRequest(identityToken, null);

        AppleJwtClaims claims = new AppleJwtClaims(appleSub, "test@example.com");
        when(appleJwtValidator.validate(identityToken, null)).thenReturn(claims);
        when(findOrCreateUserByAppleSubUseCase.exists(appleSub)).thenReturn(false);

        User newUser = User.registerWithApple(appleSub, "athlete_001234.5", "unknown", PrivacyLevel.PUBLIC, Gender.UNSPECIFIED);
        when(findOrCreateUserByAppleSubUseCase.execute(appleSub)).thenReturn(newUser);
        when(sessionAuthService.issue(newUser.getUserId())).thenReturn(sampleTokens());

        AppleSignInResponse response = controller.signInWithApple(request);

        assertNotNull(response);
        assertTrue(response.created(), "Should indicate user was created");
        assertEquals(newUser.getUserId(), response.userId());
        assertEquals("athlete_001234.5", response.username());
        assertEquals(Gender.UNSPECIFIED, response.gender());
        assertEquals("access.jwt", response.accessToken());
        assertEquals("opaque-refresh", response.refreshToken());
        assertEquals(900, response.expiresIn());

        verify(appleJwtValidator).validate(identityToken, null);
        verify(findOrCreateUserByAppleSubUseCase).exists(appleSub);
        verify(findOrCreateUserByAppleSubUseCase).execute(appleSub);
        verify(sessionAuthService).issue(newUser.getUserId());
    }

    @Test
    public void shouldReturnExistingUserOnSubsequentSignIn() {
        String identityToken = "valid.apple.jwt";
        String appleSub = "001234.567890abcdef.1234";
        AppleSignInRequest request = new AppleSignInRequest(identityToken, null);

        AppleJwtClaims claims = new AppleJwtClaims(appleSub, "test@example.com");
        when(appleJwtValidator.validate(identityToken, null)).thenReturn(claims);
        when(findOrCreateUserByAppleSubUseCase.exists(appleSub)).thenReturn(true);

        User existingUser = new User("3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3", "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE, appleSub);
        when(findOrCreateUserByAppleSubUseCase.execute(appleSub)).thenReturn(existingUser);
        when(sessionAuthService.issue(existingUser.getUserId())).thenReturn(sampleTokens());

        AppleSignInResponse response = controller.signInWithApple(request);

        assertNotNull(response);
        assertFalse(response.created(), "Should indicate user already existed");
        assertEquals("3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3", response.userId());
        assertEquals("yeikobu", response.username());
        assertEquals(Gender.MALE, response.gender());
        assertEquals(900, response.expiresIn());

        verify(sessionAuthService).issue(existingUser.getUserId());
    }

    @Test
    public void shouldValidateNonceWhenProvided() {
        String identityToken = "valid.apple.jwt";
        String nonce = "a1b2c3d4e5f6";
        String appleSub = "001234.567890abcdef.1234";
        AppleSignInRequest request = new AppleSignInRequest(identityToken, nonce);

        AppleJwtClaims claims = new AppleJwtClaims(appleSub, "test@example.com");
        when(appleJwtValidator.validate(identityToken, nonce)).thenReturn(claims);
        when(findOrCreateUserByAppleSubUseCase.exists(appleSub)).thenReturn(true);

        User user = new User("3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3", "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE, appleSub);
        when(findOrCreateUserByAppleSubUseCase.execute(appleSub)).thenReturn(user);
        when(sessionAuthService.issue(user.getUserId())).thenReturn(sampleTokens());

        AppleSignInResponse response = controller.signInWithApple(request);

        assertNotNull(response);
        verify(appleJwtValidator).validate(identityToken, nonce);
    }

    @Test
    public void shouldThrowExceptionWhenTokenIsInvalid() {
        String invalidToken = "invalid.jwt.token";
        AppleSignInRequest request = new AppleSignInRequest(invalidToken, null);

        when(appleJwtValidator.validate(invalidToken, null))
                .thenThrow(new InvalidAppleJwtException("Invalid JWT format"));

        assertThrows(InvalidAppleJwtException.class, () -> controller.signInWithApple(request));

        verify(findOrCreateUserByAppleSubUseCase, never()).exists(any());
        verify(findOrCreateUserByAppleSubUseCase, never()).execute(any());
        verify(sessionAuthService, never()).issue(any());
    }

    @Test
    public void shouldThrowExceptionWhenTokenIsExpired() {
        String expiredToken = "expired.jwt.token";
        AppleSignInRequest request = new AppleSignInRequest(expiredToken, null);

        when(appleJwtValidator.validate(expiredToken, null))
                .thenThrow(new InvalidAppleJwtException("Token has expired"));

        assertThrows(InvalidAppleJwtException.class, () -> controller.signInWithApple(request));

        verify(findOrCreateUserByAppleSubUseCase, never()).execute(any());
    }

    @Test
    public void shouldRefreshTokens() {
        when(sessionAuthService.refresh("old-refresh")).thenReturn(
                new SessionTokens("new-access", "new-refresh", 900));

        SessionTokenResponse response = controller.refresh(new RefreshTokenRequest("old-refresh"));

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        assertEquals(900, response.expiresIn());
    }
}
