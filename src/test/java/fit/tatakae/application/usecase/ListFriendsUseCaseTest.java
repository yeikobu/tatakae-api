package fit.tatakae.application.usecase;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListFriendsUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private ListFriendsUseCase useCase;

    @Test
    public void shouldResolveTheUserAtTheOtherEndOfEveryAcceptedFriendship() {
        // Arrange
        User friendOfIncomingRequest = TestUsers.user("user_2", "cl", PrivacyLevel.PUBLIC);
        User friendOfOutgoingRequest = TestUsers.user("user_3", "us", PrivacyLevel.PRIVATE);
        Friendship outgoing = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_3"), CLOCK);
        Friendship incoming = new Friendship(TestUsers.idOf("user_2"), TestUsers.idOf("user_1"), CLOCK);

        when(userRepository.existsById(TestUsers.idOf("user_1"))).thenReturn(true);
        when(friendshipRepository.findAcceptedFor(TestUsers.idOf("user_1"))).thenReturn(List.of(outgoing, incoming));
        when(userRepository.findById(TestUsers.idOf("user_3"))).thenReturn(Optional.of(friendOfOutgoingRequest));
        when(userRepository.findById(TestUsers.idOf("user_2"))).thenReturn(Optional.of(friendOfIncomingRequest));

        // Act
        List<AcceptedFriend> friends = useCase.execute(TestUsers.idOf("user_1"));

        // Assert
        assertEquals(2, friends.size());
        assertEquals(outgoing.getId(), friends.get(0).friendshipId());
        assertEquals(friendOfOutgoingRequest, friends.get(0).friend());
        assertEquals(incoming.getId(), friends.get(1).friendshipId());
        assertEquals(friendOfIncomingRequest, friends.get(1).friend());
    }

    @Test
    public void shouldSkipFriendshipsWhoseUserIsNoLongerStored() {
        // Arrange
        Friendship outgoing = new Friendship(TestUsers.idOf("user_1"), TestUsers.idOf("user_3"), CLOCK);

        when(userRepository.existsById(TestUsers.idOf("user_1"))).thenReturn(true);
        when(friendshipRepository.findAcceptedFor(TestUsers.idOf("user_1"))).thenReturn(List.of(outgoing));
        when(userRepository.findById(TestUsers.idOf("user_3"))).thenReturn(Optional.empty());

        // Act
        List<AcceptedFriend> friends = useCase.execute(TestUsers.idOf("user_1"));

        // Assert
        assertTrue(friends.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(userRepository.existsById(TestUsers.idOf("ghost"))).thenReturn(false);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(TestUsers.idOf("ghost")));
    }
}
