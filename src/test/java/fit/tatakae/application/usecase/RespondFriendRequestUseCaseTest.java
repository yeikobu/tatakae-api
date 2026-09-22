package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.application.event.FriendRequestAcceptedEvent;
import fit.tatakae.application.port.UserEventPublisher;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.FriendshipStatus;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RespondFriendRequestUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private RespondFriendRequestUseCase useCase;

    @Test
    public void shouldAcceptThePendingRequestAndPersistIt() {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        User addressee = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC);
        when(friendshipRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(friendshipRepository.save(pending)).thenReturn(pending);
        when(userRepository.findById(TestUsers.idOf("user_2"))).thenReturn(Optional.of(addressee));

        // Act
        Friendship friendship = useCase.accept(pending.getId());

        // Assert
        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        verify(friendshipRepository, times(1)).save(pending);

        ArgumentCaptor<FriendRequestAcceptedEvent> eventCaptor = ArgumentCaptor.forClass(FriendRequestAcceptedEvent.class);
        verify(userEventPublisher).publishFriendRequestAccepted(eq(TestUsers.idOf("user_1")), eventCaptor.capture());
        assertEquals(addressee, eventCaptor.getValue().acceptedBy());
        assertEquals(FriendshipStatus.ACCEPTED, eventCaptor.getValue().friendship().getStatus());
    }

    @Test
    public void shouldRejectThePendingRequestAndPersistIt() {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        when(friendshipRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(friendshipRepository.save(pending)).thenReturn(pending);

        // Act
        Friendship friendship = useCase.reject(pending.getId());

        // Assert
        assertEquals(FriendshipStatus.REJECTED, friendship.getStatus());
        verify(friendshipRepository, times(1)).save(pending);
        verify(userEventPublisher, never()).publishFriendRequestAccepted(any(), any());
    }

    @Test
    public void shouldThrowExceptionWhenAcceptingAnUnknownFriendship() {
        // Arrange
        when(friendshipRepository.findById("ghost")).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.accept("ghost"));
        verify(friendshipRepository, never()).save(any(Friendship.class));
        verify(userEventPublisher, never()).publishFriendRequestAccepted(any(), any());
    }

    @Test
    public void shouldThrowExceptionWhenRejectingAnUnknownFriendship() {
        // Arrange
        when(friendshipRepository.findById("ghost")).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.reject("ghost"));
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    public void shouldThrowExceptionWhenAddresseeMissingOnAccept() {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        when(friendshipRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(friendshipRepository.save(pending)).thenReturn(pending);
        when(userRepository.findById(TestUsers.idOf("user_2"))).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.accept(pending.getId()));
        verify(userEventPublisher, never()).publishFriendRequestAccepted(any(), any());
    }
}
