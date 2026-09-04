package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.DuplicateUserException;
import fit.tatakae.domain.exception.InvalidUserException;
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
public class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegisterUserUseCase useCase;

    @Test
    public void shouldRegisterANewAthleteWithAFreshIdentity() {
        // Arrange
        when(userRepository.findByUsername("yeikobu")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User user = useCase.execute("yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.FEMALE);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        assertNotNull(user.getUserId());
        assertEquals("yeikobu", user.getUsername());
        assertEquals("cl", captor.getValue().getCountry());
        assertEquals(PrivacyLevel.PUBLIC, user.getPrivacyLevel());
        assertEquals(Gender.FEMALE, user.getGender());
    }

    // Availability is checked against the normalized handle, so casing cannot smuggle a duplicate in.
    @Test
    public void shouldCheckAvailabilityAgainstTheNormalizedHandle() {
        // Arrange
        when(userRepository.findByUsername("yeikobu")).thenReturn(Optional.of(TestUsers.user("yeikobu")));

        // Act and Assert
        assertThrows(DuplicateUserException.class, () -> {
            useCase.execute("YEIKOBU", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleIsAlreadyTaken() {
        // Arrange
        when(userRepository.findByUsername("yeikobu")).thenReturn(Optional.of(TestUsers.user("yeikobu")));

        // Act and Assert
        assertThrows(DuplicateUserException.class, () -> {
            useCase.execute("yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldRejectAnInvalidHandleBeforeTouchingTheRepository() {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> {
            useCase.execute("jacob aguilar", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        });
        verifyNoInteractions(userRepository);
    }
}
