package fit.tatakae.infrastructure.web.security;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.ForbiddenOperationException;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.infrastructure.web.controller.UserController;
import fit.tatakae.infrastructure.web.dto.UpdateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class OwnershipValidationTest {

    private UserController userController;
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userController = new UserController(
                null, null, null, null, 
                (userId, username, country, privacy, gender) -> {
                    User user = new User(userId, username, country, privacy, gender);
                    return user;
                },
                null, null, null);
    }

    @Test
    public void shouldPreventUserFromUpdatingAnotherUsersProfile() {
        // Arrange
        String authenticatedUserId = "user-123";
        String targetUserId = "user-456";
        
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(authenticatedUserId, "apple-sub-123");
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

        UpdateUserRequest request = new UpdateUserRequest("newname", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        // Act & Assert
        assertThrows(ForbiddenOperationException.class, () -> {
            userController.update(targetUserId, request);
        });

        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldPreventUserFromDeletingAnotherUsersAccount() {
        // Arrange
        String authenticatedUserId = "user-123";
        String targetUserId = "user-456";
        
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(authenticatedUserId, "apple-sub-123");
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

        // Act & Assert
        assertThrows(ForbiddenOperationException.class, () -> {
            userController.delete(targetUserId);
        });

        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldAllowUserToUpdateOwnProfile() {
        // Arrange
        String authenticatedUserId = "user-123";
        
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(authenticatedUserId, "apple-sub-123");
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

        UpdateUserRequest request = new UpdateUserRequest("newname", "cl", PrivacyLevel.PUBLIC, Gender.MALE);

        User existingUser = new User(authenticatedUserId, "oldname", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newname")).thenReturn(Optional.empty());

        // Act - should not throw
        try {
            userController.update(authenticatedUserId, request);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
