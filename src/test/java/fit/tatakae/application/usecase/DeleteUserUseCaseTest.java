package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserUseCase useCase;

    @Test
    public void shouldDeleteAnExistingUser() {
        // Arrange
        when(userRepository.existsById(TestUsers.idOf("user_1"))).thenReturn(true);

        // Act
        useCase.execute(TestUsers.idOf("user_1"));

        // Assert
        verify(userRepository, times(1)).delete(TestUsers.idOf("user_1"));
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(userRepository.existsById(TestUsers.idOf("ghost"))).thenReturn(false);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(TestUsers.idOf("ghost")));
        verify(userRepository, never()).delete(anyString());
    }
}
