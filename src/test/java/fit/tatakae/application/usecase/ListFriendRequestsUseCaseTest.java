package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.FriendshipRepository;
import fit.tatakae.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListFriendRequestsUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private ListFriendRequestsUseCase useCase;

    @Test
    public void shouldListTheRequestsWaitingForTheUserAnswer() {
        // Arrange
        List<Friendship> expected = List.of(new Friendship(TestUsers.idOf("user_2"), TestUsers.idOf("user_1"), CLOCK));
        when(userRepository.existsById(TestUsers.idOf("user_1"))).thenReturn(true);
        when(friendshipRepository.findPendingIncoming(TestUsers.idOf("user_1"))).thenReturn(expected);

        // Act
        List<Friendship> requests = useCase.execute(TestUsers.idOf("user_1"), FriendRequestDirection.INCOMING);

        // Assert
        assertEquals(expected, requests);
    }

    @Test
    public void shouldListTheRequestsTheUserIsStillWaitingAnAnswerFor() {
        // Arrange
        List<Friendship> expected = List.of(new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK));
        when(userRepository.existsById(TestUsers.idOf("user_1"))).thenReturn(true);
        when(friendshipRepository.findPendingOutgoing(TestUsers.idOf("user_1"))).thenReturn(expected);

        // Act
        List<Friendship> requests = useCase.execute(TestUsers.idOf("user_1"), FriendRequestDirection.OUTGOING);

        // Assert
        assertEquals(expected, requests);
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(userRepository.existsById(TestUsers.idOf("ghost"))).thenReturn(false);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            useCase.execute(TestUsers.idOf("ghost"), FriendRequestDirection.INCOMING);
        });
    }
}
