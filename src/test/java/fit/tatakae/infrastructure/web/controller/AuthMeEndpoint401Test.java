package fit.tatakae.infrastructure.web.controller;

import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.application.usecase.GetUserUseCase;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.AuthenticationRequiredException;
import fit.tatakae.infrastructure.web.security.AuthenticatedUser;
import fit.tatakae.infrastructure.web.security.SessionAuthService;
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

@ExtendWith(MockitoExtension.class)
public class AuthMeEndpoint401Test {

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
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldThrowAuthenticationRequiredExceptionWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        AuthenticationRequiredException exception = assertThrows(
            AuthenticationRequiredException.class,
            () -> controller.me(),
            "Expected AuthenticationRequiredException when accessing /me without authentication"
        );

        assertEquals("Authentication required: provide a valid Bearer token", exception.getMessage());
    }

    @Test
    public void shouldReturnUserResponseWhenAuthenticated() {
        String userId = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3";
        String appleSub = "001234.567890abcdef.1234";

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, appleSub);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

        User mockUser = new User(userId, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE, appleSub);
        when(getUserUseCase.execute(userId)).thenReturn(mockUser);

        var response = controller.me();

        assertEquals(userId, response.userId());
        assertEquals("yeikobu", response.username());
        assertEquals("cl", response.country());
    }
}
