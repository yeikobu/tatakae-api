package fit.tatakae.infrastructure.web.dto;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserResponse", description = "Public view of an athlete")
public record UserResponse(

        @Schema(description = "Stable identity of the athlete, it never changes",
                example = "3f2a9c1e-6b5d-4c8a-9f11-72d0e4a1b8c3") String userId,
        @Schema(description = "Current public handle", example = "yeikobu") String username,
        @Schema(example = "cl") String country,
        @Schema(example = "PUBLIC") PrivacyLevel privacyLevel,
        @Schema(example = "MALE") Gender gender) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getCountry(),
                user.getPrivacyLevel(),
                user.getGender());
    }
}
