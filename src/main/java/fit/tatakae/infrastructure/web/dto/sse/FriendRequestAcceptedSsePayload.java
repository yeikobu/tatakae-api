package fit.tatakae.infrastructure.web.dto.sse;

import fit.tatakae.application.event.FriendRequestAcceptedEvent;
import fit.tatakae.infrastructure.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "FriendRequestAcceptedSsePayload",
        description = "SSE payload when a friend request you sent is accepted")
public record FriendRequestAcceptedSsePayload(
        @Schema(example = "6f1c1c8e-1f2a-4f2a-9a1e-2b7d9c0a1234") String id,
        @Schema(example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3") String requesterId,
        @Schema(example = "8c7b6a55-4d3e-42f1-90ab-1c2d3e4f5a6b") String addresseeId,
        UserResponse acceptedBy,
        Instant respondedAt) {

    public static FriendRequestAcceptedSsePayload from(FriendRequestAcceptedEvent event) {
        return new FriendRequestAcceptedSsePayload(
                event.friendship().getId(),
                event.friendship().getRequesterId(),
                event.friendship().getAddresseeId(),
                UserResponse.from(event.acceptedBy()),
                event.friendship().getRespondedAt());
    }
}
