package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListUsersUseCase useCase;

    @Test
    public void shouldReturnEveryStoredUser() {
        // Arrange
        List<User> expected = List.of(TestUsers.user("Jacob", "cl", PrivacyLevel.PUBLIC));
        when(userRepository.findAll()).thenReturn(expected);

        // Act
        List<User> users = useCase.execute();

        // Assert
        assertEquals(expected, users);
        verify(userRepository, times(1)).findAll();
    }
}
