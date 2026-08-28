package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.domain.entity.Friendship;
import fit.tatakae.domain.entity.FriendshipStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "FriendshipResponse", description = "State of a relation between two athletes")
public record FriendshipResponse(

        @Schema(example = "6f1c1c8e-1f2a-4f2a-9a1e-2b7d9c0a1234") String id,
        @Schema(example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3") String requesterId,
        @Schema(example = "8c7b6a55-4d3e-42f1-90ab-1c2d3e4f5a6b") String addresseeId,
        @Schema(example = "PENDING") FriendshipStatus status,
        Instant createdAt,
        Instant respondedAt) {

    public static FriendshipResponse from(Friendship friendship) {
        return new FriendshipResponse(
                friendship.getId(),
                friendship.getRequesterId(),
                friendship.getAddresseeId(),
                friendship.getStatus(),
                friendship.getCreatedAt(),
                friendship.getRespondedAt());
    }
}
