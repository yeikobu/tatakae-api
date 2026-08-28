package fit.tatakae.domain.entity;

import fit.tatakae.domain.exception.InvalidFriendshipException;
import fit.tatakae.domain.exception.InvalidFriendshipTransitionException;
import fit.tatakae.domain.exception.InvalidUserException;
import fit.tatakae.domain.exception.SelfFriendshipException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class FriendshipTest {

    private static final Instant NOW = Instant.parse("2026-08-28T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private static final String REQUESTER = "11111111-1111-1111-1111-111111111111";
    private static final String ADDRESSEE = "22222222-2222-2222-2222-222222222222";
    private static final String OUTSIDER = "33333333-3333-3333-3333-333333333333";

    @Test
    public void shouldCreateAPendingFriendshipRequest() {
        // Act
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);

        // Assert
        assertNotNull(friendship.getId());
        assertEquals(REQUESTER, friendship.getRequesterId());
        assertEquals(ADDRESSEE, friendship.getAddresseeId());
        assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
        assertEquals(NOW, friendship.getCreatedAt());
        assertNull(friendship.getRespondedAt());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"yeikobu", "not-a-uuid"})
    public void shouldThrowExceptionWhenTheRequesterIsNotAValidIdentity(String invalidRequester) {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> {
            new Friendship(invalidRequester, ADDRESSEE, CLOCK);
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"kenshin", "not-a-uuid"})
    public void shouldThrowExceptionWhenTheAddresseeIsNotAValidIdentity(String invalidAddressee) {
        // Act and Assert
        assertThrows(InvalidUserException.class, () -> {
            new Friendship(REQUESTER, invalidAddressee, CLOCK);
        });
    }

    @Test
    public void shouldThrowExceptionWhenAnAthleteBefriendsItself() {
        // Act and Assert
        assertThrows(SelfFriendshipException.class, () -> {
            new Friendship(REQUESTER, REQUESTER, CLOCK);
        });
    }

    // A UUID written in upper case is the same identity, so it cannot smuggle a self friendship in.
    @Test
    public void shouldRejectSelfFriendshipEvenWhenTheIdentityCasingDiffers() {
        // Act and Assert
        assertThrows(SelfFriendshipException.class, () -> {
            new Friendship(REQUESTER.toUpperCase(), REQUESTER, CLOCK);
        });
    }

    @Test
    public void shouldAcceptAPendingRequest() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);

        // Act
        friendship.accept();

        // Assert
        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        assertEquals(NOW, friendship.getRespondedAt());
        assertTrue(friendship.isAccepted());
    }

    @Test
    public void shouldRejectAPendingRequest() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);

        // Act
        friendship.reject();

        // Assert
        assertEquals(FriendshipStatus.REJECTED, friendship.getStatus());
        assertEquals(NOW, friendship.getRespondedAt());
        assertFalse(friendship.isAccepted());
    }

    @Test
    public void shouldThrowExceptionWhenAcceptingATwiceAnsweredRequest() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);
        friendship.accept();

        // Act and Assert
        assertThrows(InvalidFriendshipTransitionException.class, friendship::accept);
    }

    @Test
    public void shouldThrowExceptionWhenRejectingAnAlreadyAcceptedRequest() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);
        friendship.accept();

        // Act and Assert
        assertThrows(InvalidFriendshipTransitionException.class, friendship::reject);
    }

    @Test
    public void shouldBlockAPendingFriendship() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);

        // Act
        friendship.block();

        // Assert
        assertEquals(FriendshipStatus.BLOCKED, friendship.getStatus());
        assertEquals(NOW, friendship.getRespondedAt());
    }

    @Test
    public void shouldBlockAnAcceptedFriendship() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);
        friendship.accept();

        // Act
        friendship.block();

        // Assert
        assertEquals(FriendshipStatus.BLOCKED, friendship.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenBlockingARejectedFriendship() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);
        friendship.reject();

        // Act and Assert
        assertThrows(InvalidFriendshipTransitionException.class, friendship::block);
    }

    @Test
    public void shouldKnowWhichAthletesItInvolves() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);

        // Act and Assert
        assertTrue(friendship.involves(REQUESTER));
        assertTrue(friendship.involves(ADDRESSEE));
        assertFalse(friendship.involves(OUTSIDER));
    }

    @Test
    public void shouldResolveTheOtherEndOfTheFriendship() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);

        // Act and Assert
        assertEquals(ADDRESSEE, friendship.friendOf(REQUESTER));
        assertEquals(REQUESTER, friendship.friendOf(ADDRESSEE));
    }

    @Test
    public void shouldThrowExceptionWhenResolvingTheOtherEndForAnOutsider() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);

        // Act and Assert
        assertThrows(InvalidFriendshipException.class, () -> friendship.friendOf(OUTSIDER));
    }

    @Test
    public void shouldBePendingOnlyForTheAddressee() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);

        // Act and Assert
        assertTrue(friendship.isPendingFor(ADDRESSEE));
        assertFalse(friendship.isPendingFor(REQUESTER));
    }

    @Test
    public void shouldNotBePendingForTheAddresseeOnceAnswered() {
        // Arrange
        Friendship friendship = new Friendship(REQUESTER, ADDRESSEE, CLOCK);
        friendship.accept();

        // Act and Assert
        assertFalse(friendship.isPendingFor(ADDRESSEE));
    }

    @Test
    public void shouldBeReconstitutedWithItsOwnIdentity() {
        // Arrange
        String id = "friendship-1";
        Instant respondedAt = NOW.plusSeconds(120);

        // Act
        Friendship friendship =
                new Friendship(id, REQUESTER, ADDRESSEE, FriendshipStatus.ACCEPTED, NOW, respondedAt, CLOCK);

        // Assert
        assertEquals(id, friendship.getId());
        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        assertEquals(NOW, friendship.getCreatedAt());
        assertEquals(respondedAt, friendship.getRespondedAt());
    }

    @Test
    public void shouldCompareFriendshipsByIdentityOnly() {
        // Arrange
        Friendship friendship =
                new Friendship("friendship-1", REQUESTER, ADDRESSEE, FriendshipStatus.PENDING, NOW, null, CLOCK);
        Friendship sameIdentity =
                new Friendship("friendship-1", OUTSIDER, ADDRESSEE, FriendshipStatus.ACCEPTED, NOW, NOW, CLOCK);
        Friendship otherIdentity =
                new Friendship("friendship-2", REQUESTER, ADDRESSEE, FriendshipStatus.PENDING, NOW, null, CLOCK);

        // Act and Assert
        assertEquals(friendship, friendship);
        assertEquals(friendship, sameIdentity);
        assertEquals(friendship.hashCode(), sameIdentity.hashCode());
        assertNotEquals(friendship, otherIdentity);
        assertNotEquals(friendship, new Object());
    }
}
