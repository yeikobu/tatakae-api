package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
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
public class FindUserByUsernameUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FindUserByUsernameUseCase useCase;

    @Test
    public void shouldResolveAHandleIntoItsAthlete() {
        // Arrange
        User expected = TestUsers.user("yeikobu");
        when(userRepository.findByUsername("yeikobu")).thenReturn(Optional.of(expected));

        // Act
        User user = useCase.execute("YEIKOBU");

        // Assert
        assertEquals(expected, user);
        assertEquals(TestUsers.idOf("yeikobu"), user.getUserId());
    }

    @Test
    public void shouldThrowExceptionWhenNoAthleteOwnsTheHandle() {
        // Arrange
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute("nobody"));
    }
}
