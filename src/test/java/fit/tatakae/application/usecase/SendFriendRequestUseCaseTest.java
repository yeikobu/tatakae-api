package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.service.FriendshipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SendFriendRequestUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private FriendshipService friendshipService;

    @InjectMocks
    private SendFriendRequestUseCase useCase;

    @Test
    public void shouldPersistThePendingRequestCreatedByTheDomainService() {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        when(userRepository.existsById(TestUsers.idOf("user_1"))).thenReturn(true);
        when(userRepository.existsById(TestUsers.idOf("user_2"))).thenReturn(true);
        when(friendshipService.createRequest(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"))).thenReturn(pending);
        when(friendshipRepository.save(pending)).thenReturn(pending);

        // Act
        Friendship friendship = useCase.execute(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"));

        // Assert
        assertEquals(pending, friendship);
        verify(friendshipRepository, times(1)).save(pending);
    }

    @Test
    public void shouldThrowExceptionWhenRequesterDoesNotExist() {
        // Arrange
        when(userRepository.existsById(TestUsers.idOf("ghost"))).thenReturn(false);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(TestUsers.idOf("ghost"), TestUsers.idOf("user_2")));
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    public void shouldThrowExceptionWhenAddresseeDoesNotExist() {
        // Arrange
        when(userRepository.existsById(TestUsers.idOf("user_1"))).thenReturn(true);
        when(userRepository.existsById(TestUsers.idOf("ghost"))).thenReturn(false);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(TestUsers.idOf("user_1"), TestUsers.idOf("ghost")));
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }
}
