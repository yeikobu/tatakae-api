package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateFriendshipRequest", description = "Answer given to a pending friend request")
public record UpdateFriendshipRequest(

        @Schema(description = "New status of the relation", example = "ACCEPTED")
        @NotNull(message = "status is required")
        FriendshipAnswer status) {

    public enum FriendshipAnswer {
        ACCEPTED,
        REJECTED
    }
}
