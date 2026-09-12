package fit.tatakae.application.port;

import fit.tatakae.application.event.FriendRequestAcceptedEvent;
import fit.tatakae.application.event.FriendRequestReceivedEvent;
import fit.tatakae.application.event.LeaderboardUpdateEvent;

import java.util.Collection;

/**
 * Outbound port for real-time user notifications (SSE, etc.).
 * Application use cases depend on this; infrastructure supplies the transport.
 */
public interface UserEventPublisher {

    void publishFriendRequestReceived(String recipientUserId, FriendRequestReceivedEvent event);

    void publishFriendRequestAccepted(String recipientUserId, FriendRequestAcceptedEvent event);

    void publishLeaderboardUpdate(Collection<String> recipientUserIds, LeaderboardUpdateEvent event);
}
