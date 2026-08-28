package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.DuplicateUserException;
import fit.tatakae.domain.exception.InvalidUserException;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateUserUseCaseTest {

    private static final String IDENTITY = TestUsers.idOf("yeikobu");

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserUseCase useCase;

    // Renaming is the reason the identity is a UUID, so the id has to survive it untouched.
    @Test
    public void shouldRenameTheAthleteKeepingItsIdentity() {
        // Arrange
        when(userRepository.findById(IDENTITY)).thenReturn(Optional.of(TestUsers.user("yeikobu")));
        when(userRepository.findByUsername("kenshin")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User updated = useCase.execute(IDENTITY, "kenshin", "us", PrivacyLevel.PRIVATE);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        assertEquals(IDENTITY, updated.getUserId());
        assertEquals("kenshin", captor.getValue().getUsername());
        assertEquals("us", updated.getCountry());
        assertEquals(PrivacyLevel.PRIVATE, updated.getPrivacyLevel());
    }

    @Test
    public void shouldAllowKeepingTheSameHandle() {
        // Arrange
        User stored = TestUsers.user("yeikobu");
        when(userRepository.findById(IDENTITY)).thenReturn(Optional.of(stored));
        when(userRepository.findByUsername("yeikobu")).thenReturn(Optional.of(stored));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User updated = useCase.execute(IDENTITY, "yeikobu", "us", PrivacyLevel.PUBLIC);

        // Assert
        assertEquals("us", updated.getCountry());
        assertEquals("yeikobu", updated.getUsername());
    }

    @Test
    public void shouldThrowExceptionWhenTheAthleteDoesNotExist() {
        // Arrange
        String unknownIdentity = TestUsers.idOf("ghost");
        when(userRepository.findById(unknownIdentity)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            useCase.execute(unknownIdentity, "yeikobu", "cl", PrivacyLevel.PUBLIC);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenTheNewHandleBelongsToAnotherAthlete() {
        // Arrange
        when(userRepository.findById(IDENTITY)).thenReturn(Optional.of(TestUsers.user("yeikobu")));
        when(userRepository.findByUsername("kenshin")).thenReturn(Optional.of(TestUsers.user("kenshin")));

        // Act and Assert
        assertThrows(DuplicateUserException.class, () -> {
            useCase.execute(IDENTITY, "kenshin", "cl", PrivacyLevel.PUBLIC);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldRejectAnIdentityThatIsNotAUuid() {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> {
            useCase.execute("yeikobu", "kenshin", "cl", PrivacyLevel.PUBLIC);
        });
        verifyNoInteractions(userRepository);
    }
}
