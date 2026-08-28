package fit.tatakae.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateFriendshipRequest", description = "Payload used to send a friend request")
public record CreateFriendshipRequest(

        @Schema(description = "Identity of the athlete sending the request",
                example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3")
        @NotBlank(message = "requesterId is required")
        String requesterId,

        @Schema(description = "Identity of the athlete receiving the request",
                example = "8c7b6a55-4d3e-42f1-90ab-1c2d3e4f5a6b")
        @NotBlank(message = "addresseeId is required")
        String addresseeId) {
}
