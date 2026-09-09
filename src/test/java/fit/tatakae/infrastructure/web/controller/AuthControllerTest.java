package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.application.usecase.GetUserUseCase;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.dto.AppleSignInRequest;
import fit.tatakae.infrastructure.web.dto.AppleSignInResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AppleJwtValidator appleJwtValidator;

    @Mock
    private FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase;

    @Mock
    private GetUserUseCase getUserUseCase;

    private AuthController controller;

    @BeforeEach
    public void setUp() {
        controller = new AuthController(appleJwtValidator, findOrCreateUserByAppleSubUseCase, getUserUseCase);
    }

    @Test
    public void shouldCreateNewUserOnFirstSignIn() {
        // Arrange
        String identityToken = "valid.apple.jwt";
        String appleSub = "001234.567890abcdef.1234";
        AppleSignInRequest request = new AppleSignInRequest(identityToken, null);

        AppleJwtClaims claims = new AppleJwtClaims(appleSub, "test@example.com");
        when(appleJwtValidator.validate(identityToken, null)).thenReturn(claims);
        when(findOrCreateUserByAppleSubUseCase.exists(appleSub)).thenReturn(false);

        User newUser = User.registerWithApple(appleSub, "athlete_001234.5", "unknown", PrivacyLevel.PUBLIC, Gender.UNSPECIFIED);
        when(findOrCreateUserByAppleSubUseCase.execute(appleSub)).thenReturn(newUser);

        // Act
        AppleSignInResponse response = controller.signInWithApple(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.created(), "Should indicate user was created");
        assertEquals(newUser.getUserId(), response.userId());
        assertEquals("athlete_001234.5", response.username());
        assertEquals(Gender.UNSPECIFIED, response.gender());

        verify(appleJwtValidator).validate(identityToken, null);
        verify(findOrCreateUserByAppleSubUseCase).exists(appleSub);
        verify(findOrCreateUserByAppleSubUseCase).execute(appleSub);
    }

    @Test
    public void shouldReturnExistingUserOnSubsequentSignIn() {
        // Arrange
        String identityToken = "valid.apple.jwt";
        String appleSub = "001234.567890abcdef.1234";
        AppleSignInRequest request = new AppleSignInRequest(identityToken, null);

        AppleJwtClaims claims = new AppleJwtClaims(appleSub, "test@example.com");
        when(appleJwtValidator.validate(identityToken, null)).thenReturn(claims);
        when(findOrCreateUserByAppleSubUseCase.exists(appleSub)).thenReturn(true);

        User existingUser = new User("3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3", "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE, appleSub);
        when(findOrCreateUserByAppleSubUseCase.execute(appleSub)).thenReturn(existingUser);

        // Act
        AppleSignInResponse response = controller.signInWithApple(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.created(), "Should indicate user already existed");
        assertEquals("3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3", response.userId());
        assertEquals("yeikobu", response.username());
        assertEquals(Gender.MALE, response.gender());

        verify(appleJwtValidator).validate(identityToken, null);
        verify(findOrCreateUserByAppleSubUseCase).exists(appleSub);
        verify(findOrCreateUserByAppleSubUseCase).execute(appleSub);
    }

    @Test
    public void shouldValidateNonceWhenProvided() {
        // Arrange
        String identityToken = "valid.apple.jwt";
        String nonce = "a1b2c3d4e5f6";
        String appleSub = "001234.567890abcdef.1234";
        AppleSignInRequest request = new AppleSignInRequest(identityToken, nonce);

        AppleJwtClaims claims = new AppleJwtClaims(appleSub, "test@example.com");
        when(appleJwtValidator.validate(identityToken, nonce)).thenReturn(claims);
        when(findOrCreateUserByAppleSubUseCase.exists(appleSub)).thenReturn(true);

        User user = new User("3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3", "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE, appleSub);
        when(findOrCreateUserByAppleSubUseCase.execute(appleSub)).thenReturn(user);

        // Act
        AppleSignInResponse response = controller.signInWithApple(request);

        // Assert
        assertNotNull(response);
        verify(appleJwtValidator).validate(identityToken, nonce);
    }

    @Test
    public void shouldThrowExceptionWhenTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        AppleSignInRequest request = new AppleSignInRequest(invalidToken, null);

        when(appleJwtValidator.validate(invalidToken, null))
                .thenThrow(new InvalidAppleJwtException("Invalid JWT format"));

        // Act & Assert
        assertThrows(InvalidAppleJwtException.class, () -> {
            controller.signInWithApple(request);
        });

        verify(findOrCreateUserByAppleSubUseCase, never()).exists(any());
        verify(findOrCreateUserByAppleSubUseCase, never()).execute(any());
    }

    @Test
    public void shouldThrowExceptionWhenTokenIsExpired() {
        // Arrange
        String expiredToken = "expired.jwt.token";
        AppleSignInRequest request = new AppleSignInRequest(expiredToken, null);

        when(appleJwtValidator.validate(expiredToken, null))
                .thenThrow(new InvalidAppleJwtException("Token has expired"));

        // Act & Assert
        assertThrows(InvalidAppleJwtException.class, () -> {
            controller.signInWithApple(request);
        });

        verify(findOrCreateUserByAppleSubUseCase, never()).execute(any());
    }
}
