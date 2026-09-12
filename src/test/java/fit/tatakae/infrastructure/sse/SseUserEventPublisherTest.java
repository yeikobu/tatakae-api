package fit.tatakae.infrastructure.sse;

import fit.tatakae.TestUsers;
import fit.tatakae.application.event.FriendRequestAcceptedEvent;
import fit.tatakae.application.event.FriendRequestReceivedEvent;
import fit.tatakae.application.event.LeaderboardUpdateEvent;
import fit.tatakae.domain.entity.Exercise;
import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.dto.sse.FriendRequestAcceptedSsePayload;
import fit.tatakae.infrastructure.web.dto.sse.FriendRequestSsePayload;
import fit.tatakae.infrastructure.web.dto.sse.LeaderboardUpdateSsePayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SseUserEventPublisherTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private SseHub sseHub;

    @InjectMocks
    private SseUserEventPublisher publisher;

    @Test
    public void shouldPublishFriendRequestPayload() {
        User from = TestUsers.user("alice", "cl", PrivacyLevel.PUBLIC);
        Friendship friendship = new Friendship(from.getUserId(), TestUsers.idOf("bob"), CLOCK);

        publisher.publishFriendRequestReceived(
                TestUsers.idOf("bob"),
                new FriendRequestReceivedEvent(friendship, from));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(sseHub).sendToUser(eq(TestUsers.idOf("bob")), eq(SseUserEventPublisher.EVENT_FRIEND_REQUEST), payloadCaptor.capture());
        assertInstanceOf(FriendRequestSsePayload.class, payloadCaptor.getValue());
        FriendRequestSsePayload payload = (FriendRequestSsePayload) payloadCaptor.getValue();
        assertEquals(friendship.getId(), payload.id());
        assertEquals(from.getUserId(), payload.fromUser().userId());
    }

    @Test
    public void shouldPublishFriendRequestAcceptedPayload() {
        User accepter = TestUsers.user("bob", "cl", PrivacyLevel.PUBLIC);
        Friendship friendship = new Friendship(TestUsers.idOf("alice"), accepter.getUserId(), CLOCK);
        friendship.accept();

        publisher.publishFriendRequestAccepted(
                TestUsers.idOf("alice"),
                new FriendRequestAcceptedEvent(friendship, accepter));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(sseHub).sendToUser(
                eq(TestUsers.idOf("alice")),
                eq(SseUserEventPublisher.EVENT_FRIEND_REQUEST_ACCEPTED),
                payloadCaptor.capture());
        assertInstanceOf(FriendRequestAcceptedSsePayload.class, payloadCaptor.getValue());
    }

    @Test
    public void shouldPublishLeaderboardUpdatePayload() {
        User user = TestUsers.user("jacob", "cl", PrivacyLevel.PUBLIC);
        List<String> recipients = List.of(user.getUserId(), TestUsers.idOf("friend"));

        publisher.publishLeaderboardUpdate(
                recipients,
                new LeaderboardUpdateEvent(Exercise.PULL_UP, "GLOBAL", user, 20));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(sseHub).sendToUsers(eq(recipients), eq(SseUserEventPublisher.EVENT_LEADERBOARD_UPDATE), payloadCaptor.capture());
        LeaderboardUpdateSsePayload payload = (LeaderboardUpdateSsePayload) payloadCaptor.getValue();
        assertEquals(Exercise.PULL_UP, payload.exercise());
        assertEquals("GLOBAL", payload.scope());
        assertEquals(20, payload.reps());
        assertEquals(user.getUserId(), payload.userId());
    }
}
