package fit.tatakae.infrastructure.sse;

import fit.tatakae.application.event.FriendRequestAcceptedEvent;
import fit.tatakae.application.event.FriendRequestReceivedEvent;
import fit.tatakae.application.event.LeaderboardUpdateEvent;
import fit.tatakae.application.port.UserEventPublisher;
import fit.tatakae.infrastructure.web.dto.sse.FriendRequestAcceptedSsePayload;
import fit.tatakae.infrastructure.web.dto.sse.FriendRequestSsePayload;
import fit.tatakae.infrastructure.web.dto.sse.LeaderboardUpdateSsePayload;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SseUserEventPublisher implements UserEventPublisher {

    public static final String EVENT_FRIEND_REQUEST = "friend_request";
    public static final String EVENT_FRIEND_REQUEST_ACCEPTED = "friend_request_accepted";
    public static final String EVENT_LEADERBOARD_UPDATE = "leaderboard_update";

    private final SseHub sseHub;

    public SseUserEventPublisher(SseHub sseHub) {
        this.sseHub = sseHub;
    }

    @Override
    public void publishFriendRequestReceived(String recipientUserId, FriendRequestReceivedEvent event) {
        sseHub.sendToUser(recipientUserId, EVENT_FRIEND_REQUEST, FriendRequestSsePayload.from(event));
    }

    @Override
    public void publishFriendRequestAccepted(String recipientUserId, FriendRequestAcceptedEvent event) {
        sseHub.sendToUser(
                recipientUserId,
                EVENT_FRIEND_REQUEST_ACCEPTED,
                FriendRequestAcceptedSsePayload.from(event));
    }

    @Override
    public void publishLeaderboardUpdate(Collection<String> recipientUserIds, LeaderboardUpdateEvent event) {
        sseHub.sendToUsers(
                recipientUserIds,
                EVENT_LEADERBOARD_UPDATE,
                LeaderboardUpdateSsePayload.from(event));
    }
}
