package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.application.usecase.GetUserUseCase;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.AuthenticationRequiredException;
import fit.tatakae.infrastructure.web.security.AuthenticatedUser;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Tests for GET /api/v1/auth/me authentication requirements.
 * Specifically verifies that missing authentication throws AuthenticationRequiredException (401), not IllegalStateException (500).
 */
@ExtendWith(MockitoExtension.class)
public class AuthMeEndpoint401Test {

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
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldThrowAuthenticationRequiredExceptionWhenNotAuthenticated() {
        // Arrange: no authentication in SecurityContext
        SecurityContextHolder.clearContext();

        // Act & Assert: should throw AuthenticationRequiredException (401), not IllegalStateException (500)
        AuthenticationRequiredException exception = assertThrows(
            AuthenticationRequiredException.class,
            () -> controller.me(),
            "Expected AuthenticationRequiredException when accessing /me without authentication"
        );

        assertEquals("Authentication required: provide a valid Bearer token", exception.getMessage());
    }

    @Test
    public void shouldReturnUserResponseWhenAuthenticated() {
        // Arrange: mock authenticated user
        String userId = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3";
        String appleSub = "001234.567890abcdef.1234";
        
        // Set authenticated user in security context
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, appleSub);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

        User mockUser = new User(userId, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE, appleSub);
        when(getUserUseCase.execute(userId)).thenReturn(mockUser);

        // Act
        var response = controller.me();

        // Assert
        assertEquals(userId, response.userId());
        assertEquals("yeikobu", response.username());
        assertEquals("cl", response.country());
    }
}
