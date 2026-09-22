package fit.tatakae.infrastructure.web.dto.sse;

import fit.tatakae.application.event.FriendRequestReceivedEvent;
import fit.tatakae.infrastructure.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "FriendRequestSsePayload", description = "SSE payload for an incoming friend request")
public record FriendRequestSsePayload(
        @Schema(example = "6f1c1c8e-1f2a-4f2a-9a1e-2b7d9c0a1234") String id,
        UserResponse fromUser,
        Instant createdAt) {

    public static FriendRequestSsePayload from(FriendRequestReceivedEvent event) {
        return new FriendRequestSsePayload(
                event.friendship().getId(),
                UserResponse.from(event.fromUser()),
                event.friendship().getCreatedAt());
    }
}
