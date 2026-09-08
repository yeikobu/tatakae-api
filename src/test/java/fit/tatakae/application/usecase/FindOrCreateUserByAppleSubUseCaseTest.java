package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
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
public class FindOrCreateUserByAppleSubUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FindOrCreateUserByAppleSubUseCase useCase;

    @Test
    public void shouldReturnExistingUserWhenAppleSubExists() {
        // Arrange
        String appleSub = "001234.567890abcdef.1234";
        User existingUser = User.registerWithApple(appleSub, "yeikobu", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        when(userRepository.findByAppleSub(appleSub)).thenReturn(Optional.of(existingUser));

        // Act
        User user = useCase.execute(appleSub);

        // Assert
        assertNotNull(user);
        assertEquals(appleSub, user.getAppleSub());
        verify(userRepository, times(1)).findByAppleSub(appleSub);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldCreateNewUserWhenAppleSubDoesNotExist() {
        // Arrange
        String appleSub = "001234.567890abcdef.1234";
        when(userRepository.findByAppleSub(appleSub)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User user = useCase.execute(appleSub);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        User savedUser = captor.getValue();
        assertNotNull(savedUser);
        assertEquals(appleSub, savedUser.getAppleSub());
        assertTrue(savedUser.getUsername().startsWith("athlete_"));
        assertEquals("unknown", savedUser.getCountry());
        assertEquals(PrivacyLevel.PUBLIC, savedUser.getPrivacyLevel());
        assertEquals(Gender.UNSPECIFIED, savedUser.getGender());
    }

    @Test
    public void shouldGenerateUsernameFromAppleSub() {
        // Arrange
        String appleSub = "001234.567890abcdef.1234";
        when(userRepository.findByAppleSub(appleSub)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User user = useCase.execute(appleSub);

        // Assert
        assertEquals("athlete_001234.5", user.getUsername());
    }

    @Test
    public void shouldHandleShortAppleSub() {
        // Arrange
        String appleSub = "abc";
        when(userRepository.findByAppleSub(appleSub)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User user = useCase.execute(appleSub);

        // Assert
        assertEquals("athlete_abc", user.getUsername());
    }
}
