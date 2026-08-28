package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.exception.InvalidUserException;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.SessionRepository;
import fit.tatakae.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteUserUseCaseTest {

    private static final String IDENTITY = TestUsers.idOf("yeikobu");

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private DeleteUserUseCase useCase;

    // Sessions and friendships point at the athlete, so they have to go first or the delete breaks
    // referential integrity in the database.
    @Test
    public void shouldRemoveEverythingThatPointsAtTheAthleteBeforeDeletingIt() {
        // Arrange
        when(userRepository.existsById(IDENTITY)).thenReturn(true);

        // Act
        useCase.execute(IDENTITY);

        // Assert
        InOrder order = inOrder(sessionRepository, friendshipRepository, userRepository);
        order.verify(sessionRepository).deleteAllOf(IDENTITY);
        order.verify(friendshipRepository).deleteAllInvolving(IDENTITY);
        order.verify(userRepository).delete(IDENTITY);
    }

    @Test
    public void shouldThrowExceptionWhenTheAthleteDoesNotExist() {
        // Arrange
        String unknownIdentity = TestUsers.idOf("ghost");
        when(userRepository.existsById(unknownIdentity)).thenReturn(false);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(unknownIdentity));
        verify(userRepository, never()).delete(anyString());
        verifyNoInteractions(sessionRepository, friendshipRepository);
    }

    @Test
    public void shouldRejectAnIdentityThatIsNotAUuid() {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> useCase.execute("yeikobu"));
        verifyNoInteractions(userRepository, sessionRepository, friendshipRepository);
    }
}
