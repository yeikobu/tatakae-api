package fit.tatakae.domain.service;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.FriendshipStatus;
import fit.tatakae.domain.exception.DuplicateFriendshipException;
import fit.tatakae.domain.repository.FriendshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-28T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private FriendshipRepository friendshipRepository;

    private FriendshipService friendshipService;

    @BeforeEach
    public void setUp() {
        friendshipService = new FriendshipService(friendshipRepository, CLOCK);
    }

    @Test
    public void shouldCreateAPendingRequestWhenNoRelationExistsYet() {
        // Arrange
        when(friendshipRepository.findBetween(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"))).thenReturn(Optional.empty());

        // Act
        Friendship friendship = friendshipService.createRequest(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"));

        // Assert
        assertEquals(TestUsers.idOf("user_1"), friendship.getRequesterId());
        assertEquals(TestUsers.idOf("user_2"), friendship.getAddresseeId());
        assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenARequestIsAlreadyPending() {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        when(friendshipRepository.findBetween(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"))).thenReturn(Optional.of(pending));

        // Act and Assert
        assertThrows(DuplicateFriendshipException.class, () -> {
            friendshipService.createRequest(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"));
        });
    }

    @Test
    public void shouldThrowExceptionWhenUsersAreAlreadyFriends() {
        // Arrange
        Friendship accepted = new Friendship(TestUsers.idOf("user_2"), TestUsers.idOf("user_1"), CLOCK);
        accepted.accept();
        when(friendshipRepository.findBetween(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"))).thenReturn(Optional.of(accepted));

        // Act and Assert
        assertThrows(DuplicateFriendshipException.class, () -> {
            friendshipService.createRequest(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"));
        });
    }

    @Test
    public void shouldThrowExceptionWhenTheRelationIsBlocked() {
        // Arrange
        Friendship blocked = new Friendship(TestUsers.idOf("user_2"), TestUsers.idOf("user_1"), CLOCK);
        blocked.block();
        when(friendshipRepository.findBetween(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"))).thenReturn(Optional.of(blocked));

        // Act and Assert
        assertThrows(DuplicateFriendshipException.class, () -> {
            friendshipService.createRequest(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"));
        });
    }

    @Test
    public void shouldAllowANewRequestAfterAPreviousRejection() {
        // Arrange
        Friendship rejected = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"), CLOCK);
        rejected.reject();
        when(friendshipRepository.findBetween(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"))).thenReturn(Optional.of(rejected));

        // Act
        Friendship friendship = friendshipService.createRequest(TestUsers.idOf("user_1"), TestUsers.idOf("user_2"));

        // Assert
        assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
        assertNotEquals(rejected.getId(), friendship.getId());
    }
}
