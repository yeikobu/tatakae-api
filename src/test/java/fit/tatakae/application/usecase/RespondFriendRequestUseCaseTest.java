package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.FriendshipStatus;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RespondFriendRequestUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private RespondFriendRequestUseCase useCase;

    @Test
    public void shouldAcceptThePendingRequestAndPersistIt() {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        when(friendshipRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(friendshipRepository.save(pending)).thenReturn(pending);

        // Act
        Friendship friendship = useCase.accept(pending.getId());

        // Assert
        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        verify(friendshipRepository, times(1)).save(pending);
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
    }

    @Test
    public void shouldThrowExceptionWhenAcceptingAnUnknownFriendship() {
        // Arrange
        when(friendshipRepository.findById("ghost")).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.accept("ghost"));
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    public void shouldThrowExceptionWhenRejectingAnUnknownFriendship() {
        // Arrange
        when(friendshipRepository.findById("ghost")).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.reject("ghost"));
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }
}
