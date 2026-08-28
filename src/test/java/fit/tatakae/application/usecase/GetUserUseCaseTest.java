package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserUseCase useCase;

    @Test
    public void shouldReturnTheStoredUser() {
        // Arrange
        User expected = TestUsers.user("Jacob", "cl", PrivacyLevel.PUBLIC);
        when(userRepository.findById(TestUsers.idOf("user_1"))).thenReturn(Optional.of(expected));

        // Act
        User user = useCase.execute(TestUsers.idOf("user_1"));

        // Assert
        assertEquals(expected, user);
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(TestUsers.idOf("ghost"))).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(TestUsers.idOf("ghost")));
    }
}
