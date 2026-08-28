package fit.tatakae.infrastructure.persistence.adapter;

import fit.tatakae.TestUsers;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.FriendshipStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaFriendshipRepository.class, PostgresIntegrationTest.TestClockConfiguration.class})
public class JpaFriendshipRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private JpaFriendshipRepository friendshipRepository;

    @Test
    public void shouldStoreAndReadBackAPendingRequest() {
        // Arrange
        Friendship pending = new Friendship(TestUsers.idOf("a1"), TestUsers.idOf("a2"), CLOCK);

        // Act
        friendshipRepository.save(pending);
        Optional<Friendship> stored = friendshipRepository.findById(pending.getId());

        // Assert
        assertTrue(stored.isPresent());
        assertEquals(FriendshipStatus.PENDING, stored.get().getStatus());
        assertEquals(NOW, stored.get().getCreatedAt());
        assertNull(stored.get().getRespondedAt());
    }

    @Test
    public void shouldPersistTheAcceptedStatus() {
        // Arrange
        Friendship friendship = new Friendship(TestUsers.idOf("b1"), TestUsers.idOf("b2"), CLOCK);
        friendshipRepository.save(friendship);
        friendship.accept();

        // Act
        friendshipRepository.save(friendship);
        Optional<Friendship> stored = friendshipRepository.findById(friendship.getId());

        // Assert
        assertTrue(stored.isPresent());
        assertEquals(FriendshipStatus.ACCEPTED, stored.get().getStatus());
        assertEquals(NOW, stored.get().getRespondedAt());
    }

    @Test
    public void shouldFindTheRelationInBothDirections() {
        // Arrange
        Friendship outgoing = new Friendship(TestUsers.idOf("c1"), TestUsers.idOf("c2"), CLOCK);
        friendshipRepository.save(outgoing);

        // Act
        Optional<Friendship> asRequester = friendshipRepository.findBetween(TestUsers.idOf("c1"), TestUsers.idOf("c2"));
        Optional<Friendship> asAddressee = friendshipRepository.findBetween(TestUsers.idOf("c2"), TestUsers.idOf("c1"));

        // Assert
        assertEquals(outgoing, asRequester.orElseThrow());
        assertEquals(outgoing, asAddressee.orElseThrow());
    }

    @Test
    public void shouldReturnEmptyWhenNoRelationExists() {
        // Act and Assert
        assertTrue(friendshipRepository.findBetween("nobody-1", "nobody-2").isEmpty());
        assertTrue(friendshipRepository.findById("ghost").isEmpty());
    }

    @Test
    public void shouldListAcceptedFriendshipsFromBothEnds() {
        // Arrange
        Friendship outgoing = new Friendship(TestUsers.idOf("d1"), TestUsers.idOf("d2"), CLOCK);
        Friendship incoming = new Friendship(TestUsers.idOf("d3"), TestUsers.idOf("d1"), CLOCK);
        Friendship stillPending = new Friendship(TestUsers.idOf("d1"), TestUsers.idOf("d4"), CLOCK);
        outgoing.accept();
        incoming.accept();
        friendshipRepository.save(outgoing);
        friendshipRepository.save(incoming);
        friendshipRepository.save(stillPending);

        // Act
        List<Friendship> accepted = friendshipRepository.findAcceptedFor(TestUsers.idOf("d1"));

        // Assert
        assertEquals(2, accepted.size());
        assertTrue(accepted.contains(outgoing));
        assertTrue(accepted.contains(incoming));
    }

    @Test
    public void shouldSplitPendingRequestsByDirection() {
        // Arrange
        Friendship outgoing = new Friendship(TestUsers.idOf("e1"), TestUsers.idOf("e2"), CLOCK);
        Friendship incoming = new Friendship(TestUsers.idOf("e3"), TestUsers.idOf("e1"), CLOCK);
        friendshipRepository.save(outgoing);
        friendshipRepository.save(incoming);

        // Act
        List<Friendship> pendingIncoming = friendshipRepository.findPendingIncoming(TestUsers.idOf("e1"));
        List<Friendship> pendingOutgoing = friendshipRepository.findPendingOutgoing(TestUsers.idOf("e1"));

        // Assert
        assertEquals(List.of(incoming), pendingIncoming);
        assertEquals(List.of(outgoing), pendingOutgoing);
    }

    @Test
    public void shouldReturnTheNewestRelationWhenARejectedOneWasRetried() {
        // Arrange
        Friendship rejected = new Friendship(TestUsers.idOf("f1"), TestUsers.idOf("f2"), CLOCK);
        rejected.reject();
        friendshipRepository.save(rejected);
        Friendship retried = new Friendship(
                "retry-1", TestUsers.idOf("f1"), TestUsers.idOf("f2"), FriendshipStatus.PENDING, NOW.plusSeconds(3600), null, CLOCK);
        friendshipRepository.save(retried);

        // Act
        Optional<Friendship> stored = friendshipRepository.findBetween(TestUsers.idOf("f2"), TestUsers.idOf("f1"));

        // Assert
        assertEquals(retried, stored.orElseThrow());
        assertEquals(FriendshipStatus.PENDING, stored.get().getStatus());
    }

    @Test
    public void shouldDeleteAFriendship() {
        // Arrange
        Friendship friendship = new Friendship(TestUsers.idOf("g1"), TestUsers.idOf("g2"), CLOCK);
        friendshipRepository.save(friendship);

        // Act
        friendshipRepository.delete(friendship.getId());

        // Assert
        assertTrue(friendshipRepository.findById(friendship.getId()).isEmpty());
    }

    @Test
    public void shouldDropEveryRelationInvolvingOneAthleteFromBothEnds() {
        // Arrange
        Friendship outgoing = new Friendship(TestUsers.idOf("h1"), TestUsers.idOf("h2"), CLOCK);
        Friendship incoming = new Friendship(TestUsers.idOf("h3"), TestUsers.idOf("h1"), CLOCK);
        Friendship unrelated = new Friendship(TestUsers.idOf("h2"), TestUsers.idOf("h3"), CLOCK);
        friendshipRepository.save(outgoing);
        friendshipRepository.save(incoming);
        friendshipRepository.save(unrelated);

        // Act
        friendshipRepository.deleteAllInvolving(TestUsers.idOf("h1"));

        // Assert
        assertTrue(friendshipRepository.findById(outgoing.getId()).isEmpty());
        assertTrue(friendshipRepository.findById(incoming.getId()).isEmpty());
        assertTrue(friendshipRepository.findById(unrelated.getId()).isPresent());
    }
}
