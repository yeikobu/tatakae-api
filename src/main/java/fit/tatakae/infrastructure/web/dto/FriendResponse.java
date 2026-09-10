package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.application.usecase.AcceptedFriend;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FriendResponse", description = "Accepted friend plus the friendship id needed to remove it")
public record FriendResponse(

        @Schema(description = "Id of the accepted friendship, used by DELETE /api/v1/friendships/{id}",
                example = "6f1c1c8e-1f2a-4f2a-9a1e-2b7d9c0a1234")
        String friendshipId,

        @Schema(description = "Stable identity of the friend",
                example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3")
        String userId,

        @Schema(description = "Public handle of the friend", example = "yeikobu")
        String username,

        @Schema(example = "cl") String country,
        @Schema(example = "PUBLIC") PrivacyLevel privacyLevel,
        @Schema(example = "MALE") Gender gender) {

    public static FriendResponse from(AcceptedFriend acceptedFriend) {
        var friend = acceptedFriend.friend();
        return new FriendResponse(
                acceptedFriend.friendshipId(),
                friend.getUserId(),
                friend.getUsername(),
                friend.getCountry(),
                friend.getPrivacyLevel(),
                friend.getGender());
    }
}
