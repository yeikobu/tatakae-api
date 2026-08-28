package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Friendship;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetFriendshipUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private GetFriendshipUseCase useCase;

    @Test
    public void shouldReturnTheStoredFriendship() {
        // Arrange
        Friendship stored = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        when(friendshipRepository.findById(stored.getId())).thenReturn(Optional.of(stored));

        // Act
        Friendship friendship = useCase.execute(stored.getId());

        // Assert
        assertEquals(stored, friendship);
    }

    @Test
    public void shouldThrowExceptionWhenFriendshipDoesNotExist() {
        // Arrange
        when(friendshipRepository.findById("ghost")).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute("ghost"));
    }
}
